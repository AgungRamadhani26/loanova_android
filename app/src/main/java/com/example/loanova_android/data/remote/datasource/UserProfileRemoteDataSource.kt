package com.example.loanova_android.data.remote.datasource

import com.example.loanova_android.core.base.ApiResponse
import com.example.loanova_android.data.model.dto.UserProfileResponse
import com.example.loanova_android.data.remote.api.UserProfileApi
import okhttp3.RequestBody
import retrofit2.Response
import javax.inject.Inject

/**
 * DataSource untuk User Profile API operations.
 * 
 * Responsibility:
 * - Abstraksi untuk network calls terkait User Profile
 * - Bisa ditambahkan logging, retry logic, atau caching di sini
 * - Repository hanya depend ke DataSource, bukan langsung ke API
 */
class UserProfileRemoteDataSource @Inject constructor(
    private val userProfileApi: UserProfileApi
) {
    /**
     * Get current user profile.
     */
    suspend fun getMyProfile(): Response<ApiResponse<UserProfileResponse>> {
        return userProfileApi.getMyProfile()
    }
    
    /**
     * Complete user profile (first time setup).
     */
    suspend fun completeProfile(body: RequestBody): Response<ApiResponse<UserProfileResponse>> {
        return userProfileApi.completeProfile(body)
    }
    
    /**
     * Update existing user profile.
     */
    suspend fun updateProfile(body: RequestBody): Response<ApiResponse<UserProfileResponse>> {
        return userProfileApi.updateProfile(body)
    }
}
