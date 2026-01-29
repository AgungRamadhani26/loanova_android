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
    private val localDataSource: UserDao, // Injected DAO
    private val gson: Gson
) : BaseRepository(gson), IUserProfileRepository {

    override fun getMyProfile(): Flow<Resource<UserProfileResponse>> = flow {
        emit(Resource.Loading())
        
        // 1. Show Local Cache
         try {
            val localProfile = localDataSource.getMyProfile().firstOrNull()
            if (localProfile != null) {
                emit(Resource.Success(DataMappers.mapProfileEntityToResponse(localProfile)))
            }
        } catch (e: Exception) {
            // Ignore
        }

        // 2. Network Sync
        try {
            val response = api.getMyProfile()
            // Using BaseRepository Logic manually here because flows are tricky with generic handleApiResponse wrapper return
            // But we can use parseError
            val body = response.body()
            if (response.isSuccessful && body != null && body.success && body.data != null) {
                // Save to DB
                val entity = DataMappers.mapProfileResponseToEntity(body.data)
                localDataSource.insertProfile(entity)
                emit(Resource.Success(body.data))
            } else {
                // Check local before emitting error
                val localExists = localDataSource.getMyProfile().firstOrNull() != null
                if (!localExists) {
                    if (response.code() == 404) {
                        emit(Resource.Error("PROFILE_NOT_FOUND"))
                    } else if (response.isSuccessful) {
                         emit(Resource.Error(body?.message ?: "Error")) 
                    } else {
                         val errorMsg = parseError<ApiResponse<UserProfileResponse>, UserProfileResponse>(response).message ?: "Error"
                         if (errorMsg.contains("Profil belum dilengkapi") || errorMsg.contains("memuat profil")) {
                              emit(Resource.Error("PROFILE_NOT_FOUND"))
                         } else {
                              emit(Resource.Error(errorMsg))
                         }
                    }
                }
            }
        } catch (e: Exception) {
             val localExists = localDataSource.getMyProfile().firstOrNull() != null
             if (!localExists) emit(Resource.Error(e.message ?: "Unknown Error"))
        }
    }.flowOn(Dispatchers.IO)

    override fun completeProfile(request: UserProfileCompleteRequest): Flow<Resource<UserProfileResponse>> = flow {
        emit(Resource.Loading())
        try {
            val requestBody = buildMultipartBody(request)
            val response = api.completeProfile(requestBody)
            
            // Refactored to use BaseRepository helper or just logic
            val body = response.body()
            if (response.isSuccessful && body != null && body.success && body.data != null) {
                val entity = DataMappers.mapProfileResponseToEntity(body.data)
                localDataSource.insertProfile(entity)
                emit(Resource.Success(body.data))
            } else {
                emit(parseError(response))
            }
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
                emit(parseError(response))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Terjadi kesalahan koneksi"))
        }
    }.flowOn(Dispatchers.IO)

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
