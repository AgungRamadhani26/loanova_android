package com.example.loanova_android.ui.features.loan.myloans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loanova_android.core.common.Resource
import com.example.loanova_android.data.model.dto.LoanApplicationResponse
import com.example.loanova_android.domain.repository.ILoanApplicationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State untuk MyLoans Screen
 */
data class MyLoansUiState(
    val isLoading: Boolean = false,
    val loans: List<LoanApplicationResponse> = emptyList(),
    val error: String? = null,
    val isRefreshing: Boolean = false
)

/**
 * ViewModel untuk menampilkan daftar loan applications milik Customer.
 */
@HiltViewModel
class MyLoansViewModel @Inject constructor(
    private val loanApplicationRepository: ILoanApplicationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyLoansUiState())
    val uiState: StateFlow<MyLoansUiState> = _uiState.asStateFlow()

    init {
        loadMyLoans()
    }

    /**
     * Load daftar loan applications dari API.
     */
    fun loadMyLoans() {
        viewModelScope.launch {
            loanApplicationRepository.getMyApplications().collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                loans = result.data ?: emptyList(),
                                error = null,
                                isRefreshing = false
                            ) 
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                error = result.message,
                                isRefreshing = false
                            ) 
                        }
                    }
                }
            }
        }
    }

    /**
     * Refresh data (pull-to-refresh).
     */
    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        loadMyLoans()
    }

    /**
     * Clear error message.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
