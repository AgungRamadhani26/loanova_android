package com.example.loanova_android.data.remote.api

import com.example.loanova_android.core.base.ApiResponse
import com.example.loanova_android.data.model.dto.UserPlafondResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface UserPlafondApi {
    @GET("api/user-plafonds/users/{userId}/active")
    suspend fun getActiveUserPlafond(@Path("userId") userId: Long): Response<ApiResponse<UserPlafondResponse>>
}
