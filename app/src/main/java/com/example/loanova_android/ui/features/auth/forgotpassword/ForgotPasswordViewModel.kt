package com.example.loanova_android.ui.features.auth.forgotpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loanova_android.core.common.Resource
import com.example.loanova_android.domain.usecase.auth.ForgotPasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ForgotPasswordUiState - State UI untuk halaman Forgot Password.
 */
data class ForgotPasswordUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: String? = null,
    val emailSent: Boolean = false
)

/**
 * ForgotPasswordViewModel - ViewModel untuk halaman Forgot Password.
 * 
 * FLOW:
 * 1. User memasukkan email
 * 2. ViewModel memanggil ForgotPasswordUseCase
 * 3. Backend kirim email reset password
 * 4. UI menampilkan pesan sukses dan instruksi cek email
 */
@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val forgotPasswordUseCase: ForgotPasswordUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState = _uiState.asStateFlow()

    /**
     * Request forgot password - kirim link reset ke email.
     * 
     * @param email Email user yang terdaftar
     */
    fun forgotPassword(email: String) {
        // Validasi email tidak kosong
        if (email.isBlank()) {
            _uiState.update { it.copy(error = "Email wajib diisi") }
            return
        }

        // Validasi format email sederhana
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(error = "Format email tidak valid") }
            return
        }

        viewModelScope.launch {
            forgotPasswordUseCase.execute(email).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                success = resource.data,
                                emailSent = true,
                                error = null
                            ) 
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                error = resource.message ?: "Gagal mengirim email reset password"
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

    /**
     * Reset state untuk kembali ke form.
     */
    fun resetState() {
        _uiState.value = ForgotPasswordUiState()
    }
}
