package com.example.loanova_android.ui.features.plafon

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loanova_android.data.model.dto.UserPlafondResponse
import com.example.loanova_android.domain.repository.IUserPlafondRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class UserPlafondUiState {
    object Loading : UserPlafondUiState()
    data class Success(val data: UserPlafondResponse) : UserPlafondUiState()
    data class Error(val message: String) : UserPlafondUiState()
}

@HiltViewModel
class UserPlafondViewModel @Inject constructor(
    private val repository: IUserPlafondRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UserPlafondUiState>(UserPlafondUiState.Loading)
    val uiState: StateFlow<UserPlafondUiState> = _uiState.asStateFlow()

    init {
        fetchActivePlafond()
    }

    fun fetchActivePlafond() {
        viewModelScope.launch {
            _uiState.value = UserPlafondUiState.Loading
            repository.getActivePlafond().collect { result ->
                result.onSuccess { data ->
                    _uiState.value = UserPlafondUiState.Success(data)
                }.onFailure { error ->
                    _uiState.value = UserPlafondUiState.Error(error.message ?: "Terjadi kesalahan")
                }
            }
        }
    }
}
