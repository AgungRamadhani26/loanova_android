package com.example.loanova_android.data.repository

import com.example.loanova_android.data.model.dto.LoginRequest
import com.example.loanova_android.data.model.dto.LoginResponse
import com.example.loanova_android.data.remote.datasource.AuthRemoteDataSource
import com.example.loanova_android.domain.model.User
import com.example.loanova_android.domain.repository.IAuthRepository
import com.google.gson.Gson
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val remoteDataSource: AuthRemoteDataSource,
    private val gson: Gson
) : IAuthRepository {

    override suspend fun login(username: String, password: String): Result<User> {
        return try {
            val response = remoteDataSource.login(LoginRequest(username, password))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    Result.success(
                        User(
                            username = body.data.username ?: username,
                            roles = body.data.roles ?: emptyList(),
                            permissions = body.data.permissions ?: emptyList(),
                            accessToken = body.data.accessToken ?: "",
                            refreshToken = body.data.refreshToken ?: ""
                        )
                    )
                } else {
                    Result.failure(Exception(body?.message ?: "Gagal login"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorResponse = gson.fromJson(errorBody, LoginResponse::class.java)
                
                val errorMessage = when {
                    errorResponse?.data?.errors != null -> {
                        errorResponse.data.errors.values.joinToString(", ")
                    }
                    errorResponse?.message != null -> errorResponse.message
                    else -> "Username atau password salah"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
