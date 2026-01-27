package com.example.loanova_android.ui.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loanova_android.core.common.Resource
import com.example.loanova_android.data.local.TokenManager
import com.example.loanova_android.data.model.dto.UserProfileResponse
import com.example.loanova_android.domain.usecase.auth.LogoutUseCase
import com.example.loanova_android.domain.usecase.profile.GetMyProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = false, // Loading indicator for profile fetch/logout
    val userProfile: UserProfileResponse? = null,
    val isProfileNotFound: Boolean = false,
    val username: String? = null,
    val error: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getMyProfileUseCase: GetMyProfileUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        // Load username from local session
        _uiState.update { it.copy(username = tokenManager.getUsername()) }
        // Fetch profile
        fetchUserProfile()
    }

    fun fetchUserProfile() {
        viewModelScope.launch {
            getMyProfileUseCase.execute().collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                userProfile = result.data, 
                                isProfileNotFound = false
                            ) 
                        }
                    }
                    is Resource.Error -> {
                        if (result.message == "PROFILE_NOT_FOUND") {
                            _uiState.update { 
                                it.copy(
                                    isLoading = false, 
                                    userProfile = null, 
                                    isProfileNotFound = true
                                ) 
                            }
                        } else {
                            _uiState.update { 
                                it.copy(
                                    isLoading = false, 
                                    error = result.message
                                ) 
                            }
                        }
                    }
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            logoutUseCase.execute().collect { _ ->
                // Clear state on logout
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        userProfile = null,
                        isProfileNotFound = false,
                        username = null
                    ) 
                }
                // Note: Navigation back to login is usually handled by observing
                // TokenManager state in the main Activity/Navigation or HomeViewModel
                // However, since this VM is isolated, we rely on the global TokenManager
                // state change if HomeViewModel observes it, or we might need a callback.
                // But typically HomeScreen observes the session.
            }
        }
    }
}
