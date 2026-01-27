package com.example.loanova_android.ui.features.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loanova_android.core.base.ApiResponse
import com.example.loanova_android.data.model.dto.RegisterRequest
import com.example.loanova_android.data.remote.api.AuthApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authApi: AuthApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun register(request: RegisterRequest) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null, success = false)
        viewModelScope.launch {
            try {
                val response = authApi.register(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    _uiState.value = _uiState.value.copy(isLoading = false, success = true)
                } else {
                    val errorMsg = response.body()?.message ?: response.errorBody()?.string() ?: "Registrasi gagal"
                    _uiState.value = _uiState.value.copy(isLoading = false, error = errorMsg)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.localizedMessage ?: "Terjadi kesalahan")
            }
        }
    }
    
    fun resetState() {
        _uiState.value = RegisterUiState()
    }
}
