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
 * ResetPasswordUseCase - UseCase untuk reset password dengan token dari email.
 * 
 * FLOW:
 * 1. User klik link dari email (deep link)
 * 2. App extract token dari deep link
 * 3. User memasukkan password baru
 * 4. UseCase memanggil repository.resetPassword(token, newPassword)
 * 5. Backend validasi token dan update password
 * 6. Return message sukses/gagal
 * 
 * @param repository Repository interface untuk operasi autentikasi
 */
class ResetPasswordUseCase @Inject constructor(
    private val repository: IAuthRepository
) {
    /**
     * Eksekusi reset password.
     * 
     * @param token Token dari email reset password
     * @param newPassword Password baru
     * @return Flow<Resource<String>> - Message sukses/error
     */
    fun execute(token: String, newPassword: String): Flow<Resource<String>> {
        return repository.resetPassword(token, newPassword)
    }
}
