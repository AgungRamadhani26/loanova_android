package com.example.loanova_android.ui.features.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loanova_android.core.common.Resource
import com.example.loanova_android.data.model.dto.RegisterRequest
import com.example.loanova_android.domain.usecase.auth.RegisterUseCase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel untuk Halaman Registrasi.
 * 
 * Bertanggung jawab untuk:
 * 1. Menerima input form (RegisterRequest).
 * 2. Mengelola State UI (Loading, Success, Error).
 * 3. Menangani parsing error validasi dari backend (misal: "Email sudah terdaftar").
 */
@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase,
    private val gson: Gson
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    /**
     * Memulai proses registrasi.
     * 
     * @param request Data user baru (username, email, password).
     * Errors dikembalikan dalam bentuk Resource.Error yang kemudian di-parse.
     * Jika formatnya "VALIDATION_ERROR:...", maka akan dikonversi menjadi fieldErrors map.
     */
    fun register(request: RegisterRequest) {
        // Validation check is delegated to Backend/Repository for consistency
        viewModelScope.launch {
            registerUseCase.execute(request.username, request.email, request.password)
                .collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            _uiState.update { it.copy(isLoading = true, error = null, success = false, fieldErrors = null) }
                        }
                        is Resource.Success -> {
                            _uiState.update { it.copy(isLoading = false, success = true, error = null, fieldErrors = null) }
                        }
                        is Resource.Error -> {
                            val rawMsg = resource.message ?: "Registrasi gagal"
                            val msg = if (rawMsg.isBlank()) "Registrasi gagal" else rawMsg
                            
                            // Updated to match BaseRepository format: "VALIDATION_ERROR||Message||JSON"
                            if (msg.startsWith("VALIDATION_ERROR||")) {
                                try {
                                    val parts = msg.split("||")
                                    if (parts.size >= 3) {
                                        val backendMsg = parts[1]
                                        val json = parts[2]
                                        val type = object : TypeToken<Map<String, String>>() {}.type
                                        val errors: Map<String, String> = gson.fromJson(json, type)
                                        _uiState.update { it.copy(isLoading = false, error = backendMsg, fieldErrors = errors) }
                                    } else {
                                        _uiState.update { it.copy(isLoading = false, error = msg, fieldErrors = null) }
                                    }
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
    
    fun resetState() {
        _uiState.value = RegisterUiState()
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null, fieldErrors = null) }
    }
}
