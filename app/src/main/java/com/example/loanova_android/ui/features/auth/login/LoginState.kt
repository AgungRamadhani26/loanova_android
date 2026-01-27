package com.example.loanova_android.ui.features.auth.login

// ============================================================================
// LAYER: UI (Presentation Layer)
// PATTERN: UI State - Unidirectional Data Flow (UDF)
// RESPONSIBILITY: Representasi state untuk Login screen
// ============================================================================

import com.example.loanova_android.domain.model.User

/**
 * LoginUiState - Data class yang merepresentasikan state UI untuk Login screen.
 * 
 * APA ITU UI STATE?
 * - UI State adalah snapshot dari kondisi UI pada waktu tertentu
 * - Semua informasi yang diperlukan UI untuk render ada di sini
 * - ViewModel memproduksi state, UI mengkonsumsi dan me-render
 * 
 * UNIDIRECTIONAL DATA FLOW (UDF):
 * ```
 * User Action → ViewModel → Update State → UI Re-render
 *     ↑                                        │
 *     └────────────────────────────────────────┘
 * ```
 * - Data mengalir satu arah: ViewModel → UI
 * - Event mengalir balik: UI → ViewModel
 * - UI tidak pernah modify state langsung
 * 
 * KENAPA IMMUTABLE (data class dengan val)?
 * 1. PREDICTABLE: State tidak bisa diubah secara tidak sengaja
 * 2. THREAD-SAFE: Aman diakses dari berbagai thread
 * 3. EASY COMPARISON: Compose bisa detect perubahan dengan equals()
 * 4. DEBUG-FRIENDLY: State bisa di-log/capture untuk debugging
 * 
 * DEFAULT VALUES:
 * - Semua property punya default value
 * - Memungkinkan partial update dengan .copy()
 * - Initial state: tidak loading, tidak ada error, tidak ada success
 * 
 * @property isLoading true jika sedang proses login (tampilkan loading indicator)
 * @property success User object jika login berhasil (trigger navigation)
 * @property error Error message jika login gagal (tampilkan di UI)
 */
data class LoginUiState(
    val isLoading: Boolean = false,  // Flag untuk menampilkan loading spinner
    val success: User? = null,        // Null = belum login, User = login berhasil
    val error: String? = null,        // Null = tidak ada error, String = pesan error
    val fieldErrors: Map<String, String>? = null // Validation errors (key: field, value: message)
)
