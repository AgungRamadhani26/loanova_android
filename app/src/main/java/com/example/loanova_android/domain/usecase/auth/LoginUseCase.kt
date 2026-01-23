package com.example.loanova_android.domain.usecase.auth

import com.example.loanova_android.domain.model.User
import com.example.loanova_android.domain.repository.IAuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: IAuthRepository
) {
    suspend fun execute(username: String, password: String): Result<User> {
        return repository.login(username, password)
    }
}
