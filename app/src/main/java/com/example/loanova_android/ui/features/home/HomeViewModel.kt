package com.example.loanova_android.ui.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loanova_android.core.common.Resource
import com.example.loanova_android.data.local.TokenManager
import com.example.loanova_android.domain.model.Plafond
import com.example.loanova_android.domain.usecase.auth.LogoutUseCase
import com.example.loanova_android.domain.usecase.plafond.GetPublicPlafondsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.loanova_android.domain.usecase.profile.GetMyProfileUseCase
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = false,
    val plafonds: List<Plafond> = emptyList(),
    val error: String? = null,
    val isLoggedIn: Boolean = false,
    val username: String? = null,
    val hasProfile: Boolean = false,
    val isProfileLoading: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getPublicPlafondsUseCase: GetPublicPlafondsUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getMyProfileUseCase: GetMyProfileUseCase,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        checkLoginStatus()
        fetchPlafonds()
    }

    fun checkLoginStatus() {
        val isLoggedIn = tokenManager.isLoggedIn()
        _uiState.update {
            it.copy(
                isLoggedIn = isLoggedIn,
                username = tokenManager.getUsername()
            )
        }
        if (isLoggedIn) {
            fetchProfileStatus()
        } else {
            _uiState.update { it.copy(hasProfile = false) }
        }
    }

    private fun fetchProfileStatus() {
        viewModelScope.launch {
            getMyProfileUseCase.execute().collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isProfileLoading = true) }
                    }
                    is Resource.Success -> {
                        val profile = result.data
                        val isComplete = profile != null && !profile.fullName.isNullOrBlank()
                        _uiState.update { 
                            it.copy(
                                isProfileLoading = false, 
                                hasProfile = isComplete 
                            ) 
                        }
                    }
                    is Resource.Error -> {
                        // If 404 or profile not found, hasProfile = false
                        _uiState.update { 
                            it.copy(
                                isProfileLoading = false, 
                                hasProfile = false 
                            ) 
                        }
                    }
                }
            }
        }
    }

    /**
     * Menghandle aksi Logout dari UI.
     */
    fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            logoutUseCase.execute().collect { result ->
                // Whether success or fail, we clear session locally
                 _uiState.update { 
                     it.copy(
                         isLoading = false,
                         isLoggedIn = false,
                         username = null
                     ) 
                 }
            }
        }
    }

    private fun fetchPlafonds() {
        viewModelScope.launch {
            getPublicPlafondsUseCase.execute().collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _uiState.update { it.copy(isLoading = false, plafonds = result.data ?: emptyList()) }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isLoading = false, error = result.message) }
                    }
                }
            }
        }
    }
}
