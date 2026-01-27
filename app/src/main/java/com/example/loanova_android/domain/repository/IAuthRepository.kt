package com.example.loanova_android.domain.repository

// ============================================================================
// LAYER: Domain Layer
// PATTERN: Repository Interface - Clean Architecture
// PRINCIPLE: Dependency Inversion Principle (DIP)
// ============================================================================

import com.example.loanova_android.domain.model.User

import kotlinx.coroutines.flow.Flow
import com.example.loanova_android.core.common.Resource

/**
 * IAuthRepository - Interface Repository untuk operasi autentikasi.
 * 
 * MENGAPA INTERFACE DI DOMAIN LAYER?
 * - Ini adalah implementasi dari Dependency Inversion Principle (DIP)
 * - Domain Layer mendefinisikan CONTRACT (interface)
 * - Data Layer menyediakan IMPLEMENTATION (AuthRepositoryImpl)
 * - Domain Layer TIDAK bergantung pada Data Layer, tapi sebaliknya!
 * 
 * DEPENDENCY RULE DALAM CLEAN ARCHITECTURE:
 * ```
 *   UI Layer ──────> Domain Layer <────── Data Layer
 *                        │
 *                   (Interface)
 * ```
 * - Panah menunjukkan arah dependency
 * - Domain Layer adalah pusat, tidak bergantung layer lain
 * 
 * KEUNTUNGAN INTERFACE:
 * 1. TESTABILITY: Bisa di-mock untuk unit testing
 * 2. FLEXIBILITY: Bisa ganti implementasi tanpa ubah UseCase
 * 3. DECOUPLING: Domain tidak tahu detail implementasi (Retrofit, Room, etc.)
 * 
 * NAMING CONVENTION:
 * - Prefix "I" untuk Interface (IAuthRepository)
 * - Implementation suffix "Impl" (AuthRepositoryImpl)
 */
interface IAuthRepository {
    
    /**
     * Melakukan proses login ke backend.
     * 
     * CONTRACT:
     * - Implementasi WAJIB handle network call ke API
     * - Implementasi WAJIB mapping dari DTO ke Domain Model (User)
     * - Implementasi WAJIB return Result untuk error handling
     * 
     * @param username Username untuk autentikasi
     * @param password Password untuk autentikasi
     * @return Result<User> - Domain model User jika sukses, Exception jika gagal
     */
    fun login(username: String, password: String, fcmToken: String? = null): Flow<Resource<User>>
    fun logout(): Flow<Resource<Boolean>>
    fun register(username: String, email: String, password: String): Flow<Resource<Boolean>>
}
