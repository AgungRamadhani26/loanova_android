package com.example.loanova_android.data.repository

import com.example.loanova_android.core.common.Resource
import com.example.loanova_android.data.model.dto.UserProfileResponse
import com.example.loanova_android.data.remote.api.UserProfileApi
import com.example.loanova_android.domain.repository.IUserProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException
import java.io.File
import javax.inject.Inject

import com.example.loanova_android.data.model.dto.UserProfileCompleteRequest
import com.example.loanova_android.core.base.ApiResponse
import com.example.loanova_android.data.model.dto.ValidationErrorData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class UserProfileRepositoryImpl @Inject constructor(
    private val api: UserProfileApi,
    private val gson: Gson
) : IUserProfileRepository {

    override fun getMyProfile(): Flow<Resource<UserProfileResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.getMyProfile()
            val body = response.body()
            
            if (response.isSuccessful && body != null) {
                if (body.success && body.data != null) {
                    emit(Resource.Success(body.data))
                } else {
                    emit(Resource.Error(body.message))
                }
            } else {
                if (response.code() == 404) {
                    // Specific handling for Profile Not Found
                    emit(Resource.Error("PROFILE_NOT_FOUND"))
                } else {
                    emit(Resource.Error(response.message()))
                }
            }
        } catch (e: HttpException) {
            if (e.code() == 404) {
                emit(Resource.Error("PROFILE_NOT_FOUND"))
            } else {
                emit(Resource.Error(e.message ?: "Network Error"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Unknown Error"))
        }
    }.flowOn(Dispatchers.IO)

    override fun completeProfile(request: UserProfileCompleteRequest): Flow<Resource<UserProfileResponse>> = flow {
        emit(Resource.Loading())
        try {
            // Prepared Text Parts
            val textParts = mutableMapOf<String, RequestBody>()
            textParts["fullName"] = createPartFromString(request.fullName)
            textParts["phoneNumber"] = createPartFromString(request.phoneNumber)
            textParts["userAddress"] = createPartFromString(request.userAddress)
            textParts["nik"] = createPartFromString(request.nik)
            textParts["birthDate"] = createPartFromString(request.birthDate)
            if (!request.npwpNumber.isNullOrEmpty()) {
                textParts["npwpNumber"] = createPartFromString(request.npwpNumber)
            }

            // Prepared File Parts
            val ktpPart = request.ktpPhoto?.let { prepareFilePart("ktpPhoto", it) }
            val profilePart = request.profilePhoto?.let { prepareFilePart("profilePhoto", it) }
            val npwpPart = request.npwpPhoto?.let { prepareFilePart("npwpPhoto", it) }

            val response = api.completeProfile(textParts, ktpPart, profilePart, npwpPart)
            
            val body = response.body()
             if (response.isSuccessful && body != null) {
                if (body.success && body.data != null) {
                    emit(Resource.Success(body.data))
                } else {
                    emit(Resource.Error(body.message))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                if (errorBody != null) {
                    try {
                        val type = object : TypeToken<ApiResponse<ValidationErrorData>>() {}.type
                        val errorResponse: ApiResponse<ValidationErrorData> = gson.fromJson(errorBody, type)
                        
                        if (errorResponse.data?.errors != null && errorResponse.data.errors.isNotEmpty()) {
                             val errorsJson = gson.toJson(errorResponse.data.errors)
                             val backendMessage = if (errorResponse.message.isNullOrBlank()) response.message() else errorResponse.message
                             emit(Resource.Error("VALIDATION_ERROR||$backendMessage||$errorsJson"))
                             return@flow
                        } else {
                             // Fallback
                             val msg = if (errorResponse.message.isNotEmpty()) errorResponse.message else response.message()
                             emit(Resource.Error(msg))
                             return@flow
                        }
                    } catch (e: Exception) {
                        emit(Resource.Error(response.message()))
                    }
                } else {
                    emit(Resource.Error(response.message()))
                }
            }

        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Terjadi kesalahan koneksi"))
        }
    }.flowOn(Dispatchers.IO)

    private fun createPartFromString(value: String): RequestBody {
        return RequestBody.create("text/plain".toMediaTypeOrNull(), value)
    }

    private fun prepareFilePart(partName: String, file: File): MultipartBody.Part {
        val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
        return MultipartBody.Part.createFormData(partName, file.name, requestFile)
    }
}
