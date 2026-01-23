package com.example.loanova_android.data.remote.datasource

import com.example.loanova_android.data.model.dto.LoginRequest
import com.example.loanova_android.data.model.dto.LoginResponse
import com.example.loanova_android.data.remote.api.AuthApi
import retrofit2.Response
import javax.inject.Inject

class AuthRemoteDataSource @Inject constructor(
    private val authApi: AuthApi
) {
    suspend fun login(request: LoginRequest): Response<LoginResponse> {
        return authApi.login(request)
    }
}
