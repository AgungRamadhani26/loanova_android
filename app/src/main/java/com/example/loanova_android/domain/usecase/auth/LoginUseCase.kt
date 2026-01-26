package com.example.loanova_android.domain.usecase.auth

// ============================================================================
// LAYER: Domain Layer
// PATTERN: UseCase (Interactor) - Clean Architecture
// ============================================================================

import com.example.loanova_android.domain.model.User
import com.example.loanova_android.domain.repository.IAuthRepository
import javax.inject.Inject

/**
 * LoginUseCase - UseCase untuk menangani business logic proses login.
 * 
 * APA ITU USECASE?
 * - UseCase (atau Interactor) adalah komponen di Domain Layer yang 
 *   mengenkapsulasi satu business action/operation tertentu
 * - Setiap UseCase hanya memiliki SATU tanggung jawab (Single Responsibility Principle)
 * - UseCase ini hanya melakukan: "Proses login user"
 * 
 * MENGAPA USECASE PENTING?
 * 1. ABSTRAKSI: ViewModel tidak perlu tahu detail implementasi login
 * 2. TESTABILITY: UseCase mudah di-unit test karena tidak ada dependency ke Android framework
 * 3. REUSABILITY: UseCase bisa dipakai di multiple ViewModels jika diperlukan
 * 4. MAINTAINABILITY: Jika logika login berubah, cukup ubah di satu tempat
 * 
 * CLEAN ARCHITECTURE RULES:
 * - Domain Layer adalah CORE dari aplikasi
 * - UseCase hanya bergantung pada Repository INTERFACE (bukan implementation)
 * - UseCase tidak tahu apakah data dari API, Database, atau sumber lain
 * 
 * DEPENDENCY INJECTION:
 * - @Inject constructor: Hilt akan menyediakan IAuthRepository secara otomatis
 * - Repository yang di-inject adalah INTERFACE, bukan concrete class
 * 
 * @param repository Repository interface untuk operasi autentikasi
 */
class LoginUseCase @Inject constructor(
    private val repository: IAuthRepository // Interface, bukan AuthRepositoryImpl!
) {
    /**
     * Eksekusi proses login.
     * 
     * FLOW:
     * 1. Terima username dan password dari ViewModel
     * 2. Delegasikan ke Repository untuk eksekusi actual login
     * 3. Return Result<User> ke ViewModel
     * 
     * KENAPA SUSPEND FUNCTION?
     * - Login adalah operasi async (network call)
     * - Suspend function memungkinkan eksekusi tanpa blocking main thread
     * - Caller (ViewModel) akan memanggil ini dari coroutine
     * 
     * KENAPA RETURN Result<User>?
     * - Result adalah Kotlin wrapper untuk success/failure
     * - Memungkinkan error handling yang bersih tanpa try-catch di ViewModel
     * 
     * @param username Username yang akan di-authenticate
     * @param password Password user
     * @param fcmToken FCM Token (Optional)
     * @return Result<User> - Success dengan User object, atau Failure dengan exception
     */
    suspend fun execute(username: String, password: String, fcmToken: String? = null): Result<User> {
        // Delegasikan ke Repository - UseCase ini hanya orchestrator
        // Business logic lebih kompleks bisa ditambahkan di sini jika diperlukan
        // Contoh: validasi format email, password strength check, dll.
        return repository.login(username, password, fcmToken)
    }
}
