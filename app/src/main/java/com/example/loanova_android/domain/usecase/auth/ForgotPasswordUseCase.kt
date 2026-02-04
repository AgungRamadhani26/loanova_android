package com.example.loanova_android.domain.usecase.auth

// ============================================================================
// LAYER: Domain Layer
// PATTERN: UseCase (Interactor) - Clean Architecture
// ============================================================================

import com.example.loanova_android.domain.repository.IAuthRepository
import kotlinx.coroutines.flow.Flow
import com.example.loanova_android.core.common.Resource
import javax.inject.Inject

/**
 * ForgotPasswordUseCase - UseCase untuk request reset password via email.
 * 
 * FLOW:
 * 1. User memasukkan email
 * 2. UseCase memanggil repository.forgotPassword(email)
 * 3. Backend kirim email dengan link reset password
 * 4. Return message sukses/gagal
 * 
 * @param repository Repository interface untuk operasi autentikasi
 */
class ForgotPasswordUseCase @Inject constructor(
    private val repository: IAuthRepository
) {
    /**
     * Eksekusi request forgot password.
     * 
     * @param email Email user yang terdaftar
     * @return Flow<Resource<String>> - Message sukses/error
     */
    fun execute(email: String): Flow<Resource<String>> {
        return repository.forgotPassword(email)
    }
}
