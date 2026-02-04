package com.example.loanova_android.ui.features.auth.resetpassword

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loanova_android.core.common.Resource
import com.example.loanova_android.domain.usecase.auth.ResetPasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ResetPasswordUiState - State UI untuk halaman Reset Password.
 */
data class ResetPasswordUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: String? = null,
    val passwordReset: Boolean = false,
    val token: String = ""
)

/**
 * ResetPasswordViewModel - ViewModel untuk halaman Reset Password.
 * 
 * FLOW:
 * 1. ViewModel menerima token dari navigation argument (deep link)
 * 2. User memasukkan password baru
 * 3. ViewModel memanggil ResetPasswordUseCase
 * 4. Backend validasi token dan update password
 * 5. UI menampilkan pesan sukses dan redirect ke login
 */
@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val resetPasswordUseCase: ResetPasswordUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResetPasswordUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // Get token from navigation argument (dari deep link)
        val token = savedStateHandle.get<String>("token") ?: ""
        _uiState.update { it.copy(token = token) }
    }

    /**
     * Set token manually (untuk fallback manual input).
     */
    fun setToken(token: String) {
        _uiState.update { it.copy(token = token) }
    }

    /**
     * Reset password dengan token dan password baru.
     * 
     * @param newPassword Password baru
     * @param confirmPassword Konfirmasi password baru
     */
    fun resetPassword(newPassword: String, confirmPassword: String) {
        val token = _uiState.value.token

        // Validasi token
        if (token.isBlank()) {
            _uiState.update { it.copy(error = "Token tidak valid. Silakan klik ulang link dari email.") }
            return
        }

        // Validasi password tidak kosong
        if (newPassword.isBlank()) {
            _uiState.update { it.copy(error = "Password baru wajib diisi") }
            return
        }

        // Validasi password minimal 8 karakter
        if (newPassword.length < 8) {
            _uiState.update { it.copy(error = "Password minimal 8 karakter") }
            return
        }

        // Validasi password harus mengandung huruf besar, kecil, dan angka
        val hasUpperCase = newPassword.any { it.isUpperCase() }
        val hasLowerCase = newPassword.any { it.isLowerCase() }
        val hasDigit = newPassword.any { it.isDigit() }
        
        if (!hasUpperCase || !hasLowerCase || !hasDigit) {
            _uiState.update { 
                it.copy(error = "Password harus mengandung huruf besar, huruf kecil, dan angka") 
            }
            return
        }

        // Validasi konfirmasi password
        if (newPassword != confirmPassword) {
            _uiState.update { it.copy(error = "Konfirmasi password tidak cocok") }
            return
        }

        viewModelScope.launch {
            resetPasswordUseCase.execute(token, newPassword).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                success = resource.data,
                                passwordReset = true,
                                error = null
                            ) 
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                error = resource.message ?: "Gagal reset password"
                            ) 
                        }
                    }
                }
            }
        }
    }

    /**
     * Clear error message.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
