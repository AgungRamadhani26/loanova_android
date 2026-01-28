package com.example.loanova_android.ui.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loanova_android.core.common.Resource
import com.example.loanova_android.data.model.dto.UserProfileResponse
import com.example.loanova_android.domain.repository.IUserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import com.example.loanova_android.data.model.dto.UserProfileCompleteRequest

/**
 * State UI untuk layar Lengkapi Profil.
 * @property isLoading Menandakan proses pengiriman data sedang berlangsung.
 * @property success Menampung respon sukses dari server jika berhasil.
 * @property error Pesan error umum (misal: "Koneksi Gagal").
 * @property fieldErrors Map error spesifik per kolom (misal: "phoneNumber" -> "Nomor sudah terdaftar").
 */
data class CompleteProfileUiState(
    val isLoading: Boolean = false,
    val success: UserProfileResponse? = null,
    val error: String? = null,
    val fieldErrors: Map<String, String>? = null
)



/**
 * ViewModel untuk CompleteProfileScreen.
 * Bertanggung jawab untuk:
 * 1. Mengelola state form (loading, success, error).
 * 2. Memanggil Repository untuk mengirim data ke API.
 * 3. Memparsing pesan error dari Backend (terutama validasi field).
 */
@HiltViewModel
class CompleteProfileViewModel @Inject constructor(
    private val repository: IUserProfileRepository,
    private val gson: com.google.gson.Gson
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompleteProfileUiState())
    val uiState: StateFlow<CompleteProfileUiState> = _uiState.asStateFlow()

    /**
     * Mengirim data profil ke server.
     * @param request Data lengkap user termasuk file foto (Multipart).
     */
    fun completeProfile(request: UserProfileCompleteRequest) {
        viewModelScope.launch {
            // Reset state menjadi initial sebelum request dimulai
            _uiState.update { it.copy(isLoading = true, error = null, fieldErrors = null) }
            
            repository.completeProfile(request).collect { result ->
                when (result) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true, error = null) }
                    is Resource.Success -> _uiState.update { it.copy(isLoading = false, success = result.data) }
                    is Resource.Error -> {
                         val rawMsg = result.message ?: "Gagal melengkapi profil"
                         val msg = if (rawMsg.isBlank()) "Gagal melengkapi profil" else rawMsg
                         
                         // LOGIC KHUSUS: Parsing Error Validasi dari Backend
                         // Format Backend: "VALIDATION_ERROR||Pesan Umum||JSON_FIELD_ERRORS"
                         // Contoh: "VALIDATION_ERROR||Data tidak valid||{\"phoneNumber\":\"Sudah dipakai\"}"
                        if (msg.startsWith("VALIDATION_ERROR")) {
                            try {
                                val parts = msg.split("||")
                                if (parts.size >= 3) {
                                    val backendMsg = parts[1] // "Data tidak valid"
                                    val json = parts[2]       // JSON String
                                    val type = object : com.google.gson.reflect.TypeToken<Map<String, String>>() {}.type
                                    
                                    // Convert JSON ke Map<String, String>
                                    val errors: Map<String, String> = gson.fromJson(json, type)
                                    
                                    // Update state dengan error spesifik per field
                                    _uiState.update { it.copy(isLoading = false, error = backendMsg, fieldErrors = errors) }
                                } else {
                                    // Fallback jika format tidak sesuai (kurang dari 3 bagian)
                                     _uiState.update { it.copy(isLoading = false, error = msg, fieldErrors = null) }
                                }
                            } catch (e: Exception) {
                                // Fallback jika parsing JSON gagal
                                _uiState.update { it.copy(isLoading = false, error = msg, fieldErrors = null) }
                            }
                        } else {
                            // Error biasa (bukan validasi), misal: 500 Server Error
                            _uiState.update { it.copy(isLoading = false, error = msg, fieldErrors = null) }
                        }
                    }
                }
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null, fieldErrors = null) }
    }
    
    fun resetState() {
         _uiState.update { CompleteProfileUiState() }
    }
}
