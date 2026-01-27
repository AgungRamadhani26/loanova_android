package com.example.loanova_android.data.remote.api

import com.example.loanova_android.core.base.ApiResponse
import com.example.loanova_android.data.model.dto.UserProfileResponse
import retrofit2.Response
import retrofit2.http.GET

interface UserProfileApi {
    @GET("api/user-profiles/me")
    suspend fun getMyProfile(): Response<ApiResponse<UserProfileResponse>>
}
