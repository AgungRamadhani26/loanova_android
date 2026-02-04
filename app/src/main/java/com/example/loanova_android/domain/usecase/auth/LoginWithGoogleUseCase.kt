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
 * LoginWithGoogleUseCase - UseCase untuk menangani business logic login dengan Google.
 * 
 * FLOW:
 * 1. Android Sign-In dengan Google via Firebase Auth & Credential Manager
 * 2. Dapatkan Firebase ID Token
 * 3. Kirim ID Token ke backend via Repository
 * 4. Backend verifikasi token dan create/link user
 * 5. Return JWT tokens seperti login biasa
 * 
 * @param repository Repository interface untuk operasi autentikasi
 */
class LoginWithGoogleUseCase @Inject constructor(
    private val repository: IAuthRepository
) {
    /**
     * Eksekusi proses login dengan Google.
     * 
     * @param idToken Firebase ID Token dari Google Sign-In
     * @param fcmToken FCM Token untuk push notification (optional)
     * @return Flow<Resource<User>> - Stream hasil login
     */
    fun execute(idToken: String, fcmToken: String? = null): Flow<Resource<User>> {
        return repository.loginWithFirebaseGoogle(idToken, fcmToken)
    }
}
