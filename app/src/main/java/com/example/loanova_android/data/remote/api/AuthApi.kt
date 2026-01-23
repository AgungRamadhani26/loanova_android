package com.example.loanova_android.data.remote.api

import com.example.loanova_android.data.model.dto.LoginRequest
import com.example.loanova_android.data.model.dto.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>
}
