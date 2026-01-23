package com.example.loanova_android.domain.repository

import com.example.loanova_android.domain.model.User

interface IAuthRepository {
    suspend fun login(username: String, password: String): Result<User>
}
