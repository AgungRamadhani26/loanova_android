package com.example.loanova_android.data.remote.datasource

import com.example.loanova_android.core.base.ApiResponse
import com.example.loanova_android.data.model.dto.UserPlafondResponse
import com.example.loanova_android.data.remote.api.UserPlafondApi
import retrofit2.Response
import javax.inject.Inject

/**
 * DataSource untuk User Plafond API operations.
 * 
 * Responsibility:
 * - Abstraksi untuk network calls terkait User Plafond
 * - Bisa ditambahkan logging, retry logic, atau caching di sini
 * - Repository hanya depend ke DataSource, bukan langsung ke API
 */
class UserPlafondRemoteDataSource @Inject constructor(
    private val userPlafondApi: UserPlafondApi
) {
    /**
     * Get active plafond for a specific user.
     */
    suspend fun getActiveUserPlafond(userId: Long): Response<ApiResponse<UserPlafondResponse>> {
        return userPlafondApi.getActiveUserPlafond(userId)
    }
}
