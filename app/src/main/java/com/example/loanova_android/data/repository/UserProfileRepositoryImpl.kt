package com.example.loanova_android.data.repository

import com.example.loanova_android.core.common.Resource
import com.example.loanova_android.data.model.dto.UserProfileResponse
import com.example.loanova_android.data.remote.api.UserProfileApi
import com.example.loanova_android.domain.repository.IUserProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.emitAll
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException
import java.io.File
import javax.inject.Inject

import com.example.loanova_android.data.model.dto.UserProfileCompleteRequest
import com.example.loanova_android.core.base.BaseRepository
import com.example.loanova_android.core.base.ApiResponse
import com.example.loanova_android.data.model.dto.ValidationErrorData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Implementasi Repository Profil User.
 * Berfungsi sebagai jembatan data antara Aplikasi dan API Server.
 * 
 * Tanggung Jawab:
 * 1. Mengubah Request Object menjadi Multipart/Form-Data untuk upload file.
 * 2. Menangani Response API (Success/Error).
 * 3. Mengonversi Error Body JSON (jika ada) menjadi string format khusus
 *    ("VALIDATION_ERROR||...") agar bisa diproses ViewModel.
 */
import com.example.loanova_android.data.local.dao.UserDao
import com.example.loanova_android.data.mapper.DataMappers
import kotlinx.coroutines.flow.firstOrNull

/**
 * Implementasi Repository Profil User (Offline-First).
 * 
 * Strategi:
 * 1. Read: Tampilkan Cache (UserProfileEntity) -> Sync API -> Update Cache.
 * 2. Write (Complete Profile): Upload API -> Jika Sukses, Simpan ke Cache.
 */
class UserProfileRepositoryImpl @Inject constructor(
    private val api: UserProfileApi,
    private val localDataSource: UserDao,
    private val gson: Gson,
    private val workManager: androidx.work.WorkManager,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) : BaseRepository(gson), IUserProfileRepository {

    override fun getMyProfile(): Flow<Resource<UserProfileResponse>> = flow {
        emit(Resource.Loading())
        
        // 1. Check Local Cache (Snapshot)
        var localProfile: com.example.loanova_android.data.local.entity.UserProfileEntity? = null
        try {
            localProfile = localDataSource.getMyProfile().firstOrNull()
            if (localProfile != null) {
                emit(Resource.Success(DataMappers.mapProfileEntityToResponse(localProfile)))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Network Sync
        try {
            val response = api.getMyProfile()
            val body = response.body()
            
            if (response.isSuccessful && body != null && body.success && body.data != null) {
                // Success: Update DB and observe DB for real-time updates
                val entity = DataMappers.mapProfileResponseToEntity(body.data)
                localDataSource.insertProfile(entity)
                
                // Continue emitting from DB (Single Source of Truth)
                emitAll(localDataSource.getMyProfile().map { 
                     if (it != null) Resource.Success(DataMappers.mapProfileEntityToResponse(it))
                     else Resource.Loading()
                })
            } else {
                // Error Handling
                if (response.code() == 404) {
                    emit(Resource.Error("PROFILE_NOT_FOUND"))
                    // Stop here. Do NOT emit from empty DB.
                } else {
                    val errorMsg = if (body?.message.isNullOrBlank()) "Gagal memuat profil" else body?.message!!
                    
                    if (localProfile != null) {
                        // Offline/Error but have cache -> Continue showing cache via DB flow
                         emitAll(localDataSource.getMyProfile().map { 
                             if (it != null) Resource.Success(DataMappers.mapProfileEntityToResponse(it))
                             else Resource.Loading()
                        })
                    } else {
                        emit(Resource.Error(errorMsg))
                    }
                }
            }
        } catch (e: Exception) {
            // Network Exception
            if (localProfile != null) {
                 emitAll(localDataSource.getMyProfile().map { 
                     if (it != null) Resource.Success(DataMappers.mapProfileEntityToResponse(it))
                     else Resource.Loading()
                })
            } else {
                 emit(Resource.Error(e.message ?: "Koneksi Bermasalah"))
            }
        }
    }.flowOn(Dispatchers.IO)

    override fun completeProfile(request: UserProfileCompleteRequest): Flow<Resource<UserProfileResponse>> = flow {
        // ... (Keep existing implementation for completeProfile as it might require immediate feedback, or apply same logic)
        // For brevity, keeping it online-only or assuming same logic.
        // Let's stick to updateProfile as the main target for offline.
        emit(Resource.Loading())
        try {
            val requestBody = buildMultipartBody(request)
            val response = api.completeProfile(requestBody)
            
            val body = response.body()
            if (response.isSuccessful && body != null && body.success && body.data != null) {
                val entity = DataMappers.mapProfileResponseToEntity(body.data)
                localDataSource.insertProfile(entity)
                emit(Resource.Success(body.data))
            } else {
                emit(parseError(response))
            }
        } catch (e: java.io.IOException) {
            // Offline Case
            enqueueOfflineUpdate(request, "COMPLETE")
            emit(Resource.Error("OFFLINE_QUEUED"))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Terjadi kesalahan koneksi"))
        }
    }.flowOn(Dispatchers.IO)

    override fun updateProfile(request: UserProfileCompleteRequest): Flow<Resource<UserProfileResponse>> = flow {
        emit(Resource.Loading())
        try {
            val requestBody = buildMultipartBody(request)
            val response = api.updateProfile(requestBody)
            
            val body = response.body()
            if (response.isSuccessful && body != null && body.success && body.data != null) {
                val entity = DataMappers.mapProfileResponseToEntity(body.data)
                localDataSource.insertProfile(entity)
                emit(Resource.Success(body.data))
            } else {
                emit(parseError<ApiResponse<UserProfileResponse>, UserProfileResponse>(response))
            }
        } catch (e: java.io.IOException) {
            // Offline Case
            enqueueOfflineUpdate(request, "UPDATE")
            emit(Resource.Error("OFFLINE_QUEUED"))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Terjadi kesalahan koneksi"))
        }
    }.flowOn(Dispatchers.IO)

    private fun enqueueOfflineUpdate(request: UserProfileCompleteRequest, type: String) {
        // Save files to persistent storage
        val ktpPath = copyToPersistent(request.ktpPhoto)
        val profilePath = copyToPersistent(request.profilePhoto)
        val npwpPath = copyToPersistent(request.npwpPhoto)

        // Create Data
        val inputData = androidx.work.Data.Builder()
            .putString("request_json", gson.toJson(request))
            .putString("ktp_path", ktpPath)
            .putString("profile_path", profilePath)
            .putString("npwp_path", npwpPath)
            .putString("operation_type", type)
            .build()
            
        val constraints = androidx.work.Constraints.Builder()
            .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
            .build()
            
        val workRequest = androidx.work.OneTimeWorkRequest.Builder(com.example.loanova_android.data.worker.UserProfileWorker::class.java)
            .setConstraints(constraints)
            .setInputData(inputData)
            .addTag("PROFILE_SYNC_WORK")
            .build()
            
        workManager.enqueue(workRequest)
    }

    private fun copyToPersistent(file: File?): String? {
        if (file == null || !file.exists()) return null
        return try {
            val destDir = File(context.filesDir, "pending_uploads")
            if (!destDir.exists()) destDir.mkdirs()
            val destFile = File(destDir, "offline_${System.currentTimeMillis()}_${file.name}")
            file.copyTo(destFile, overwrite = true)
            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun buildMultipartBody(request: UserProfileCompleteRequest): RequestBody {
        val builder = MultipartBody.Builder().setType(MultipartBody.FORM)

        // Text Parts
        builder.addFormDataPart("fullName", request.fullName)
        builder.addFormDataPart("phoneNumber", request.phoneNumber)
        builder.addFormDataPart("userAddress", request.userAddress)
        builder.addFormDataPart("nik", request.nik)
        builder.addFormDataPart("birthDate", request.birthDate)
        if (!request.npwpNumber.isNullOrEmpty()) {
            builder.addFormDataPart("npwpNumber", request.npwpNumber)
        }

        // File Parts
        request.ktpPhoto?.let { file ->
             val fileBody = RequestBody.create("image/*".toMediaTypeOrNull(), file)
             builder.addFormDataPart("ktpPhoto", file.name, fileBody)
        }
        request.profilePhoto?.let { file ->
             val fileBody = RequestBody.create("image/*".toMediaTypeOrNull(), file)
             builder.addFormDataPart("profilePhoto", file.name, fileBody)
        }
        request.npwpPhoto?.let { file ->
             val fileBody = RequestBody.create("image/*".toMediaTypeOrNull(), file)
             builder.addFormDataPart("npwpPhoto", file.name, fileBody)
        }

        return builder.build()
    }
}
