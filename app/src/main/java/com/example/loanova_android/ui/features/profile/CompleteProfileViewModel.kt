package com.example.loanova_android.ui.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
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
    val fieldErrors: Map<String, String>? = null,
    
    // Form Data
    val fullName: String = "",
    val phoneNumber: String = "",
    val userAddress: String = "",
    val nik: String = "",
    val birthDate: String = "",
    val npwpNumber: String = "",
    val ktpPhoto: File? = null,
    val profilePhoto: File? = null,
    val npwpPhoto: File? = null
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
    private val gson: com.google.gson.Gson,
    private val workManager: androidx.work.WorkManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompleteProfileUiState())
    val uiState: StateFlow<CompleteProfileUiState> = _uiState.asStateFlow()

    init {
        // 1. Observe DB (Existing) - Works if cache exists
        viewModelScope.launch {
            repository.getMyProfile().collect { result ->
                if (result is Resource.Success && result.data != null) {
                    onProfileSynced(result.data)
                }
            }
        }

        // 2. Observe Worker (New) - Works even if cache was empty and DB flow closed
        viewModelScope.launch {
            workManager.getWorkInfosByTagFlow("PROFILE_SYNC_WORK")
                .collect { workInfos ->
                    val successWork = workInfos.find { it.state == androidx.work.WorkInfo.State.SUCCEEDED }
                    if (successWork != null) {
                        // Worker finished successfully! Force UI update.
                        // We can manually trigger success or fetch data again.
                        // Since Worker wrote to DB, let's try fetching fresh data explicitly
                        // or just tell UI "We are good".
                         repository.getMyProfile().collect { result ->
                            if (result is Resource.Success && result.data != null) {
                                onProfileSynced(result.data)
                            }
                        }
                    }
                }
        }
    }
    
    private fun onProfileSynced(data: UserProfileResponse) {
        _uiState.update { currentState ->
            if (currentState.error == "Koneksi terputus. Data disimpan dan akan diupload saat online.") {
                 currentState.copy(
                    isLoading = false,
                    error = null,
                    success = data
                )
            } else {
                currentState
            }
        }
    }

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
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                success = result.data, 
                                error = null 
                            )
                        }
                    }
                    is Resource.Error -> {
                         val rawMsg = result.message ?: "Gagal melengkapi profil"
                         val msg = if (rawMsg.isBlank()) "Gagal melengkapi profil" else rawMsg
                         
                        if (msg == "OFFLINE_QUEUED") {
                            // Offline Success Case
                            _uiState.update { it.copy(isLoading = false, error = "Koneksi terputus. Data disimpan dan akan diupload saat online.", fieldErrors = null) }
                        } else if (msg.startsWith("VALIDATION_ERROR")) {
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
