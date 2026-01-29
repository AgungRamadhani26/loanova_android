package com.example.loanova_android.ui.features.profile.edit

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loanova_android.core.common.Resource
import com.example.loanova_android.data.model.dto.UserProfileCompleteRequest
import com.example.loanova_android.domain.repository.IUserProfileRepository
import com.example.loanova_android.ui.features.profile.CompleteProfileUiState
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val repository: IUserProfileRepository,
    private val gson: Gson
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompleteProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadCurrentProfile()
    }

    private fun loadCurrentProfile() {
        viewModelScope.launch {
            repository.getMyProfile().collect { result ->
                if (result is Resource.Success && result.data != null) {
                    val profile = result.data
                    _uiState.update { 
                        it.copy(
                            fullName = profile.fullName,
                            phoneNumber = profile.phoneNumber,
                            userAddress = profile.userAddress,
                            nik = profile.nik,
                            birthDate = profile.birthDate,
                            npwpNumber = profile.npwpNumber ?: ""
                            // Note: Photos are not loaded back into "File" objects because they are URLs now.
                            // The UI should show existing photos if no new photo is selected. 
                            // In this simple implementation, we assume user re-uploads if they want to change.
                        )
                    }
                }
            }
        }
    }

    // Input handlers (Identical to CompleteProfileViewModel)
    fun onFullNameChange(value: String) { _uiState.update { it.copy(fullName = value) } }
    fun onPhoneNumberChange(value: String) { _uiState.update { it.copy(phoneNumber = value) } }
    fun onUserAddressChange(value: String) { _uiState.update { it.copy(userAddress = value) } }
    fun onNikChange(value: String) { _uiState.update { it.copy(nik = value) } }
    fun onBirthDateChange(value: String) { _uiState.update { it.copy(birthDate = value) } }
    fun onNpwpNumberChange(value: String) { _uiState.update { it.copy(npwpNumber = value) } }

    // File handlers
    fun onKtpPhotoSelected(file: File) { _uiState.update { it.copy(ktpPhoto = file) } }
    fun onProfilePhotoSelected(file: File) { _uiState.update { it.copy(profilePhoto = file) } }
    fun onNpwpPhotoSelected(file: File) { _uiState.update { it.copy(npwpPhoto = file) } }
    fun onClearError() { _uiState.update { it.copy(error = null, fieldErrors = emptyMap()) } }

    fun updateProfile() {
        viewModelScope.launch {
            val request = UserProfileCompleteRequest(
                fullName = _uiState.value.fullName,
                phoneNumber = _uiState.value.phoneNumber,
                userAddress = _uiState.value.userAddress,
                nik = _uiState.value.nik,
                birthDate = _uiState.value.birthDate,
                npwpNumber = _uiState.value.npwpNumber,
                
                // Only include files if they are newly selected (not null)
                // Backend treats null files as "keep existing" for updates (if supported) 
                // BUT current DTO says @ValidFile(required=false) so it's safe.
                ktpPhoto = _uiState.value.ktpPhoto,
                profilePhoto = _uiState.value.profilePhoto,
                npwpPhoto = _uiState.value.npwpPhoto
            )

            repository.updateProfile(request).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null, fieldErrors = emptyMap()) }
                    }
                    is Resource.Success -> {
                        _uiState.update { it.copy(isLoading = false, success = result.data) }
                    }
                    is Resource.Error -> {
                        val messageFn = result.message ?: "Unknown Error"
                        val errors = parseValidationErrors(messageFn)
                        
                        if (errors.isNotEmpty()) {
                            _uiState.update { it.copy(isLoading = false, error = "Validasi Gagal", fieldErrors = errors) }
                        } else {
                            _uiState.update { it.copy(isLoading = false, error = messageFn) }
                        }
                    }
                }
            }
        }
    }

    private fun parseValidationErrors(message: String): Map<String, String> {
        if (message.startsWith("VALIDATION_ERROR||")) {
            try {
                val parts = message.split("||")
                if (parts.size >= 3) {
                    val json = parts[2]
                    val type = object : TypeToken<Map<String, String>>() {}.type
                    return gson.fromJson(json, type)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return emptyMap()
    }
}
