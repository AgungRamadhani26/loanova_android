package com.example.loanova_android.ui.features.auth.login

// ============================================================================
// LAYER: UI (Presentation Layer)
// PATTERN: MVVM (Model-View-ViewModel)
// ============================================================================

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loanova_android.domain.usecase.auth.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resumeWithException
import javax.inject.Inject

/**
 * LoginViewModel - ViewModel untuk mengelola state dan logic pada halaman Login.
 * 
 * PERAN DALAM MVVM:
 * - ViewModel bertindak sebagai penghubung antara View (LoginScreen) dan Model (Domain Layer)
 * - Menyimpan dan mengelola UI state yang survive configuration changes (rotasi layar)
 * - Tidak memiliki referensi langsung ke View (Composable), hanya expose state via StateFlow
 * 
 * DEPENDENCY INJECTION:
 * - @HiltViewModel: Anotasi dari Hilt untuk membuat ViewModel injectable
 * - @Inject constructor: Hilt akan menyediakan LoginUseCase secara otomatis
 * 
 * CLEAN ARCHITECTURE:
 * - ViewModel hanya bergantung pada UseCase (Domain Layer), bukan Repository langsung
 * - Ini mengikuti Dependency Rule: UI Layer -> Domain Layer
 * 
 * @param loginUseCase UseCase yang menangani business logic untuk proses login
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase, // Dependency dari Domain Layer
    private val gson: com.google.gson.Gson
) : ViewModel() {

    // ========================================================================
    // STATE MANAGEMENT dengan StateFlow
    // ========================================================================
    
    /**
     * _uiState adalah MutableStateFlow yang menyimpan state internal.
     * - MutableStateFlow: Flow yang dapat diubah nilainya (mutable)
     * - Private: Hanya ViewModel yang bisa mengubah state
     * - Hot stream: Selalu memiliki nilai terbaru dan emit ke semua collectors
     */
    private val _uiState = MutableStateFlow(LoginUiState())
    
    /**
     * uiState adalah versi read-only dari _uiState yang di-expose ke UI.
     * - asStateFlow(): Mengkonversi MutableStateFlow menjadi read-only StateFlow
     * - UI (LoginScreen) hanya bisa membaca, tidak bisa mengubah state langsung
     * - Ini mengimplementasikan prinsip Unidirectional Data Flow (UDF)
     */
    val uiState = _uiState.asStateFlow()

    // ========================================================================
    // LOGIN FUNCTION
    // ========================================================================
    
    /**
     * Fungsi untuk melakukan proses login.
     * 
     * FLOW EKSEKUSI:
     * 1. Validasi input (username & password tidak boleh kosong)
     * 2. Set loading state = true
     * 3. Panggil LoginUseCase untuk eksekusi business logic
     * 4. Handle hasil (success atau failure)
     * 5. Update UI state sesuai hasil
     * 
     * @param username Username yang diinput user
     * @param password Password yang diinput user
     */
    fun login(username: String, password: String) {
        // STEP 1: Validasi di-handle oleh Backend untuk konsistensi error message
        // Kita langsung panggil API meskipun kosong, agar response error sesuai Web
        
        // STEP 2-5: Eksekusi login dalam coroutine
        // viewModelScope: CoroutineScope yang terikat lifecycle ViewModel
        // Jika ViewModel di-destroy, semua coroutine otomatis di-cancel
        viewModelScope.launch {
            // STEP 2: Set loading state, clear previous error
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            // STEP 2.5: Get FCM Token
            var fcmToken: String? = null
            try {
                fcmToken = com.google.firebase.messaging.FirebaseMessaging.getInstance().token.await()
                android.util.Log.d("LoginViewModel", "FCM Token Fetched: $fcmToken")
            } catch (e: Exception) {
                android.util.Log.e("LoginViewModel", "Failed to fetch FCM token", e)
                e.printStackTrace()
            }

            // STEP 3: Panggil UseCase - ini akan memanggil Repository -> DataSource -> API
            // Observe Flow dari UseCase
            loginUseCase.execute(username, password, fcmToken).collect { resource ->
                when (resource) {
                    is com.example.loanova_android.core.common.Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is com.example.loanova_android.core.common.Resource.Success -> {
                        _uiState.update { it.copy(isLoading = false, success = resource.data) }
                    }
                    is com.example.loanova_android.core.common.Resource.Error -> {
                        val msg = resource.message ?: "Login gagal"
                        if (msg.startsWith("VALIDATION_ERROR:")) {
                            try {
                                val json = msg.substring("VALIDATION_ERROR:".length)
                                val type = object : com.google.gson.reflect.TypeToken<Map<String, String>>() {}.type
                                val errors: Map<String, String> = gson.fromJson(json, type)
                                _uiState.update { it.copy(isLoading = false, error = "Validasi gagal", fieldErrors = errors) }
                            } catch (e: Exception) {
                                _uiState.update { it.copy(isLoading = false, error = "Terjadi kesalahan validasi", fieldErrors = null) }
                            }
                        } else {
                            _uiState.update { it.copy(isLoading = false, error = msg, fieldErrors = null) }
                        }
                    }
                }
            }
        }
    }


    // Helper extension to await Firebase task
    private suspend fun <T> com.google.android.gms.tasks.Task<T>.await(): T {
        return kotlinx.coroutines.suspendCancellableCoroutine { cont ->
            addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    cont.resume(task.result) { cancellation -> 
                         // Handle cancellation if needed, or leave empty 
                    }
                } else {
                    cont.resumeWithException(task.exception ?: Exception("Unknown task exception"))
                }
            }
        }
    }

    // ========================================================================
    // UTILITY FUNCTIONS
    // ========================================================================
    
    /**
     * Fungsi untuk menghapus error message dari state.
     * Dipanggil ketika user mulai mengetik di text field.
     * Memberikan feedback visual bahwa error sudah di-acknowledge.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
