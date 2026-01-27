package com.example.loanova_android.data.repository

import com.example.loanova_android.core.common.Resource
import com.example.loanova_android.data.model.dto.UserProfileResponse
import com.example.loanova_android.data.remote.api.UserProfileApi
import com.example.loanova_android.domain.repository.IUserProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.HttpException
import javax.inject.Inject

class UserProfileRepositoryImpl @Inject constructor(
    private val api: UserProfileApi
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
}
