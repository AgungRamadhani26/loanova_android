package com.example.loanova_android.data.repository

import com.example.loanova_android.data.model.dto.UserPlafondResponse
import com.example.loanova_android.data.remote.api.UserPlafondApi
import com.example.loanova_android.domain.repository.IUserProfileRepository
import com.example.loanova_android.domain.repository.IUserPlafondRepository
import com.example.loanova_android.core.common.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.filter
import javax.inject.Inject

class UserPlafondRepositoryImpl @Inject constructor(
    private val api: UserPlafondApi,
    private val userProfileRepository: IUserProfileRepository
) : IUserPlafondRepository {

    override fun getActivePlafond(): Flow<Result<UserPlafondResponse>> = flow {
        try {
            // 1. Fetch User Profile to get ID (as it is not stored in TokenManager)
            // Usage of filter is crucial because Repository emits Loading first.
            val profileResult = userProfileRepository.getMyProfile()
                .filter { it !is Resource.Loading } // Skip Loading state
                .first()
            
            if (profileResult is Resource.Success && profileResult.data != null) {
                val userId = profileResult.data.userId
                
                // 2. Fetch Active Plafond using User ID
                val response = api.getActiveUserPlafond(userId!!)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.success && body.data != null) {
                        emit(Result.success(body.data))
                    } else {
                        emit(Result.failure(Exception(body.message ?: "Data plafond kosong")))
                    }
                } else {
                    emit(Result.failure(Exception("Gagal mengambil data plafond: ${response.message()}")))
                }
            } else if (profileResult is Resource.Error) {
                emit(Result.failure(Exception(profileResult.message ?: "Gagal memuat profil user")))
            } else {
                 emit(Result.failure(Exception("Profil user tidak ditemukan")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
