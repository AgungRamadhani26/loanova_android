package com.example.loanova_android.domain.usecase.auth

// ============================================================================
// LAYER: Domain Layer
// PATTERN: UseCase (Interactor) - Clean Architecture
// ============================================================================

import com.example.loanova_android.domain.model.User
import com.example.loanova_android.domain.repository.IAuthRepository
import kotlinx.coroutines.flow.Flow
import com.example.loanova_android.core.common.Resource
import javax.inject.Inject

/**
 * SignInWithGoogleUseCase - Complete Google Sign-In flow UseCase.
 * 
 * FLOW (semua di Data Layer, bukan UI):
 * 1. Terima Google ID Token dari Credential Manager (dari UI)
 * 2. Sign-In ke Firebase dengan Google credential (di Repository)
 * 3. Dapatkan Firebase ID Token (di Repository)
 * 4. Kirim Firebase ID Token ke backend (di Repository)
 * 5. Return User dengan JWT tokens
 * 
 * KENAPA BUTUH USECASE BARU?
 * - LoginWithGoogleUseCase sudah punya Firebase ID Token
 * - SignInWithGoogleUseCase dimulai dari Google ID Token (belum Firebase)
 * - Memisahkan tanggung jawab & single responsibility
 * 
 * @param repository Repository interface untuk operasi autentikasi
 */
class SignInWithGoogleUseCase @Inject constructor(
    private val repository: IAuthRepository
) {
    /**
     * Eksekusi complete Google Sign-In flow.
     * 
     * @param googleIdToken Google ID Token dari Credential Manager
     * @return Flow<Resource<User>> - Stream hasil sign-in
     */
    fun execute(googleIdToken: String): Flow<Resource<User>> {
        return repository.signInWithGoogle(googleIdToken)
    }
}
