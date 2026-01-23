package com.example.loanova_android.ui.features.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loanova_android.domain.usecase.auth.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(error = "Username dan password tidak boleh kosong") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = loginUseCase.execute(username, password)
            
            result.onSuccess { user ->
                _uiState.update { it.copy(isLoading = false, success = user) }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message ?: "Login gagal") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
