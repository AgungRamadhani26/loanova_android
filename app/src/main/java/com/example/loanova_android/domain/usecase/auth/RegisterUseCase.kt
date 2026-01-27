package com.example.loanova_android.domain.usecase.auth

import com.example.loanova_android.core.common.Resource
import com.example.loanova_android.domain.repository.IAuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val repository: IAuthRepository
) {
    fun execute(username: String, email: String, password: String): Flow<Resource<Boolean>> {
        return repository.register(username, email, password)
    }
}
