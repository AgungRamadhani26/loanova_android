package com.example.loanova_android.domain.model

// ============================================================================
// LAYER: Domain Layer
// TYPE: Domain Model (Business Entity)
// ============================================================================

/**
 * User - Domain Model yang merepresentasikan user dalam aplikasi.
 * 
 * APA ITU DOMAIN MODEL?
 * - Domain Model adalah representasi data di level business logic
 * - Berbeda dengan DTO (dari API) atau Entity (untuk Database)
 * - Domain Model hanya berisi data yang RELEVAN untuk business logic
 * 
 * PERBEDAAN DENGAN DTO dan ENTITY:
 * ```
 * DTO (Data Layer)      ->  User (Domain)    ->  UI State (UI Layer)
 * LoginResponseData.kt      User.kt              LoginUiState.kt
 * - Raw API response        - Clean business     - UI specific
 * - Snake_case fields         entity             - Display formatting
 * - Nullable fields         - Required fields    - Loading states
 * ```
 * 
 * KEUNTUNGAN DOMAIN MODEL TERPISAH:
 * 1. UI Layer tidak perlu tahu struktur API response
 * 2. Perubahan API tidak langsung mempengaruhi Domain & UI
 * 3. Business rules bisa di-enforce di domain model
 * 
 * DATA CLASS:
 * - Immutable by design (val, bukan var)
 * - Auto-generated equals(), hashCode(), copy(), toString()
 * - Thread-safe karena immutable
 * 
 * @property username Username user yang berhasil login
 * @property roles Daftar role user (e.g., "ADMIN", "USER")
 * @property permissions Daftar permission user untuk authorization
 * @property accessToken JWT access token untuk API authentication
 * @property refreshToken Token untuk refresh access token yang expired
 */
data class User(
    val username: String,       // Identifier unik user
    val roles: List<String>,    // Role-based access control
    val permissions: List<String>, // Fine-grained permissions
    val accessToken: String,    // Short-lived token untuk API calls
    val refreshToken: String    // Long-lived token untuk refresh
)
