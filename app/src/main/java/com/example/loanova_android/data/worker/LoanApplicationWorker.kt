package com.example.loanova_android.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.loanova_android.data.local.dao.LoanApplicationDao
import com.example.loanova_android.data.mapper.DataMappers
import com.example.loanova_android.data.model.dto.LoanApplicationRequest
import com.example.loanova_android.data.remote.api.LoanApplicationApi
import com.google.gson.Gson
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

/**
 * Worker untuk mengirim Loan Application saat offline.
 * Akan dieksekusi otomatis ketika device kembali online.
 * 
 * Flow:
 * 1. Deserialize request data dari InputData
 * 2. Rebuild file references dari persistent storage
 * 3. Build multipart request body
 * 4. Call API
 * 5. Jika sukses, simpan ke local DB dan show notification
 * 6. Jika gagal (5xx), retry. Jika (4xx), fail permanently.
 */
@HiltWorker
class LoanApplicationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val api: LoanApplicationApi,
    private val loanApplicationDao: LoanApplicationDao,
    private val gson: Gson
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // 1. Deserialize Input Data
            val requestJson = inputData.getString("request_json") ?: return@withContext Result.failure()
            val savingBookPath = inputData.getString("saving_book_path")
            val payslipPath = inputData.getString("payslip_path")

            // Parse text data (File fields will be null from JSON)
            val tempRequest = gson.fromJson(requestJson, LoanApplicationRequest::class.java)
            
            // Rebuild request with actual file references
            val savingBookFile = if (savingBookPath != null) File(savingBookPath) else null
            val payslipFile = if (payslipPath != null) File(payslipPath) else null
            
            // Validate files exist
            if (savingBookFile == null || !savingBookFile.exists()) {
                return@withContext Result.failure()
            }
            if (payslipFile == null || !payslipFile.exists()) {
                return@withContext Result.failure()
            }
            
            val finalRequest = tempRequest.copy(
                savingBookCover = savingBookFile,
                payslipPhoto = payslipFile
            )

            // 2. Build Multipart Body
            val requestBody = buildMultipartBody(finalRequest)

            // 3. Call API
            val response = api.submitLoanApplication(requestBody)

            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    // 4. Update Local DB
                    val entity = DataMappers.mapLoanApplicationResponseToEntity(data)
                    loanApplicationDao.insert(entity)
                    
                    // 5. Cleanup temporary files
                    cleanupTempFiles(savingBookPath, payslipPath)
                    
                    // 6. Show notification
                    showNotification(
                        "Pengajuan Pinjaman Berhasil", 
                        "Pengajuan pinjaman Anda telah dikirim dan sedang diproses."
                    )
                    
                    return@withContext Result.success()
                }
            }
            
            // Handle HTTP errors
            val responseCode = response.code()
            if (responseCode in 500..599) {
                // Server error - retry
                return@withContext Result.retry()
            }
            
            // Client error (4xx) - fail permanently
            // Clean up files even on failure
            cleanupTempFiles(savingBookPath, payslipPath)
            
            showNotification(
                "Pengajuan Pinjaman Gagal",
                "Terjadi kesalahan saat mengirim pengajuan. Silakan coba lagi."
            )
            
            return@withContext Result.failure()

        } catch (e: java.io.IOException) {
            // Network error - retry
            e.printStackTrace()
            return@withContext Result.retry()
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext Result.retry()
        }
    }

    private fun cleanupTempFiles(vararg paths: String?) {
        paths.filterNotNull().forEach { path ->
            try {
                val file = File(path)
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "loan_application_channel"
        val notificationId = 201

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "Loan Applications",
                android.app.NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifikasi untuk pengajuan pinjaman"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = androidx.core.app.NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_send)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun buildMultipartBody(request: LoanApplicationRequest): RequestBody {
        val builder = MultipartBody.Builder().setType(MultipartBody.FORM)

        // Text Parts
        builder.addFormDataPart("branchId", request.branchId.toString())
        builder.addFormDataPart("amount", request.amount)
        builder.addFormDataPart("tenor", request.tenor.toString())
        builder.addFormDataPart("occupation", request.occupation)
        builder.addFormDataPart("rekeningNumber", request.rekeningNumber)
        builder.addFormDataPart("latitude", request.latitude.toString())
        builder.addFormDataPart("longitude", request.longitude.toString())
        
        // Optional field
        if (!request.companyName.isNullOrBlank()) {
            builder.addFormDataPart("companyName", request.companyName)
        }

        // File Parts
        val savingBookBody = request.savingBookCover.asRequestBody("image/*".toMediaTypeOrNull())
        builder.addFormDataPart("savingBookCover", request.savingBookCover.name, savingBookBody)
        
        val payslipBody = request.payslipPhoto.asRequestBody("image/*".toMediaTypeOrNull())
        builder.addFormDataPart("payslipPhoto", request.payslipPhoto.name, payslipBody)

        return builder.build()
    }
}
