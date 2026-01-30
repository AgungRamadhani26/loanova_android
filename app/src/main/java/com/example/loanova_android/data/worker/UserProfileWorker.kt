package com.example.loanova_android.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.loanova_android.data.local.dao.UserDao
import com.example.loanova_android.data.mapper.DataMappers
import com.example.loanova_android.data.model.dto.UserProfileCompleteRequest
import com.example.loanova_android.data.remote.api.UserProfileApi
import com.google.gson.Gson
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

@HiltWorker
class UserProfileWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val api: UserProfileApi,
    private val userDao: UserDao,
    private val gson: Gson
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // 1. Deserialize Input Data
            val requestJson = inputData.getString("request_json") ?: return@withContext Result.failure()
            val ktpPath = inputData.getString("ktp_path")
            val profilePath = inputData.getString("profile_path")
            val npwpPath = inputData.getString("npwp_path")
            val operationType = inputData.getString("operation_type") ?: "UPDATE"

            // Parse text data
            // Note: UserProfileCompleteRequest has File fields which GSON might skip or nullify if not in JSON.
            // We reconstruct the request carefully.
            val tempRequest = gson.fromJson(requestJson, UserProfileCompleteRequest::class.java)
            
            val finalRequest = tempRequest.copy(
                ktpPhoto = if (ktpPath != null) File(ktpPath) else null,
                profilePhoto = if (profilePath != null) File(profilePath) else null,
                npwpPhoto = if (npwpPath != null) File(npwpPath) else null
            )

            // 2. Build Multipart Body (Same logic as Repository)
            val requestBody = buildMultipartBody(finalRequest)

            // 3. Call API
            val response = if (operationType == "COMPLETE") {
                api.completeProfile(requestBody)
            } else {
                api.updateProfile(requestBody)
            }

            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    // 4. Update Local DB
                    val entity = DataMappers.mapProfileResponseToEntity(data)
                    userDao.insertProfile(entity)
                    
                    showNotification("Update Profil Berhasil", "Data profil Anda telah disinkronisasi.")
                    
                    return@withContext Result.success()
                }
            }
            
            // If API failed (4xy, 5xy), we might want to retry (Result.retry()) or fail.
            // For simplicity, retry on 500s or network issues (which CoroutineWorker handles implicitly for IO exceptions usually, 
            // but Explicit Result.retry() is better for known temporary failures).
             if (response.code() in 500..599) {
                 return@withContext Result.retry()
             }

            return@withContext Result.failure()

        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext Result.retry()
        }
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "profile_update_channel"
        val notificationId = 101

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "Profile Updates",
                android.app.NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = androidx.core.app.NotificationCompat.Builder(applicationContext, channelId)
            // Assuming we have a mipmap icon, or use android default
            .setSmallIcon(android.R.drawable.ic_menu_upload) 
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun buildMultipartBody(request: UserProfileCompleteRequest): RequestBody {
        val builder = MultipartBody.Builder().setType(MultipartBody.FORM)

        builder.addFormDataPart("fullName", request.fullName)
        builder.addFormDataPart("phoneNumber", request.phoneNumber)
        builder.addFormDataPart("userAddress", request.userAddress)
        builder.addFormDataPart("nik", request.nik)
        builder.addFormDataPart("birthDate", request.birthDate)
        if (!request.npwpNumber.isNullOrEmpty()) {
            builder.addFormDataPart("npwpNumber", request.npwpNumber)
        }

        request.ktpPhoto?.let { file ->
            if (file.exists()) {
                val fileBody = RequestBody.create("image/*".toMediaTypeOrNull(), file)
                builder.addFormDataPart("ktpPhoto", file.name, fileBody)
            }
        }
        request.profilePhoto?.let { file ->
            if (file.exists()) {
                val fileBody = RequestBody.create("image/*".toMediaTypeOrNull(), file)
                builder.addFormDataPart("profilePhoto", file.name, fileBody)
            }
        }
        request.npwpPhoto?.let { file ->
             if (file.exists()) {
                val fileBody = RequestBody.create("image/*".toMediaTypeOrNull(), file)
                builder.addFormDataPart("npwpPhoto", file.name, fileBody)
             }
        }

        return builder.build()
    }
}
