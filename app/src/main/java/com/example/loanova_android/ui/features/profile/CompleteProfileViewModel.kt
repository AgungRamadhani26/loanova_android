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

data class CompleteProfileUiState(
    val isLoading: Boolean = false,
    val success: UserProfileResponse? = null,
    val error: String? = null,
    val fieldErrors: Map<String, String>? = null
)



@HiltViewModel
class CompleteProfileViewModel @Inject constructor(
    private val repository: IUserProfileRepository,
    private val gson: com.google.gson.Gson
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompleteProfileUiState())
    val uiState: StateFlow<CompleteProfileUiState> = _uiState.asStateFlow()

    fun completeProfile(request: UserProfileCompleteRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, fieldErrors = null) }
            
            repository.completeProfile(request).collect { result ->
                when (result) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true, error = null) }
                    is Resource.Success -> _uiState.update { it.copy(isLoading = false, success = result.data) }
                    is Resource.Error -> {
                         val rawMsg = result.message ?: "Gagal melengkapi profil"
                         val msg = if (rawMsg.isBlank()) "Gagal melengkapi profil" else rawMsg
                         
                        if (msg.startsWith("VALIDATION_ERROR")) {
                            try {
                                val parts = msg.split("||")
                                if (parts.size >= 3) {
                                    val backendMsg = parts[1]
                                    val json = parts[2]
                                    val type = object : com.google.gson.reflect.TypeToken<Map<String, String>>() {}.type
                                    val errors: Map<String, String> = gson.fromJson(json, type)
                                    _uiState.update { it.copy(isLoading = false, error = backendMsg, fieldErrors = errors) }
                                } else {
                                    // Fallback if format is unexpected, just show the original message
                                     _uiState.update { it.copy(isLoading = false, error = msg, fieldErrors = null) }
                                }
                            } catch (e: Exception) {
                                _uiState.update { it.copy(isLoading = false, error = msg, fieldErrors = null) }
                            }
                        } else {
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
