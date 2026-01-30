package com.example.loanova_android.ui.features.profile.password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loanova_android.core.common.Resource
import com.example.loanova_android.data.local.TokenManager
import com.example.loanova_android.data.local.dao.UserDao
import com.example.loanova_android.data.model.dto.ChangePasswordRequest
import com.example.loanova_android.domain.repository.IAuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChangePasswordUiState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val fieldErrors: Map<String, String> = emptyMap(),
    val isSuccess: Boolean = false
)

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val authRepository: IAuthRepository,
    private val tokenManager: TokenManager,
    private val userDao: UserDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChangePasswordUiState())
    val uiState = _uiState.asStateFlow()

    fun onCurrentPasswordChanged(value: String) {
        _uiState.update { it.copy(currentPassword = value, error = null, fieldErrors = it.fieldErrors - "currentPassword") }
    }

    fun onNewPasswordChanged(value: String) {
        _uiState.update { it.copy(newPassword = value, error = null, fieldErrors = it.fieldErrors - "newPassword") }
    }

    fun changePassword() {
        val currentState = _uiState.value
        val errors = validate(currentState.currentPassword, currentState.newPassword)

        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(fieldErrors = errors, error = "Validasi gagal") }
            return
        }



        viewModelScope.launch {
            val request = ChangePasswordRequest(
                currentPassword = currentState.currentPassword,
                newPassword = currentState.newPassword
            )

            authRepository.changePassword(request).collect { result ->
                when (result) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true, error = null) }
                    is Resource.Success -> {
                        // Success! Logout user.
                        tokenManager.clearSession()
                        userDao.clearProfile()
                        userDao.clearAll()
                        _uiState.update { it.copy(isLoading = false, isSuccess = true, error = null) }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isLoading = false, error = result.message) }
                    }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun validate(current: String, new: String): Map<String, String> {
        val errors = mutableMapOf<String, String>()

        if (current.isBlank()) {
            errors["currentPassword"] = "Password lama wajib diisi"
        }

        if (new.isBlank()) {
            errors["newPassword"] = "Password baru wajib diisi"
        } else {
            if (new.length < 8) {
                errors["newPassword"] = "Minimal 8 karakter"
            }
            // Strong Password Validation (Matches Backend)
            // Backend @StrongPassword: Uppercase, Lowercase, Number, Symbol
            val hasUppercase = new.any { it.isUpperCase() }
            val hasLowercase = new.any { it.isLowerCase() }
            val hasDigit = new.any { it.isDigit() }
            val hasSymbol = new.any { !it.isLetterOrDigit() }

            if (!hasUppercase || !hasLowercase || !hasDigit || !hasSymbol) {
                errors["newPassword"] = "Harus mengandung Huruf Besar, Kecil, Angka, & Simbol"
            }
        }

        return errors
    }
}
