package com.example.loanova_android.ui.features.loan.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loanova_android.core.common.Resource
import com.example.loanova_android.data.model.dto.ApplicationHistoryResponse
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
 * UI State untuk Loan History Screen
 */
data class LoanHistoryUiState(
    val isLoading: Boolean = false,
    val isLoadingHistory: Boolean = false,
    val error: String? = null,
    val loans: List<LoanApplicationResponse> = emptyList(),
    val latestLoan: LoanApplicationResponse? = null,
    val history: List<ApplicationHistoryResponse> = emptyList()
)

/**
 * ViewModel untuk Loan History Screen.
 * Mengambil loan terakhir dari customer dan menampilkan history-nya.
 */
@HiltViewModel
class LoanHistoryViewModel @Inject constructor(
    private val repository: ILoanApplicationRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LoanHistoryUiState())
    val uiState: StateFlow<LoanHistoryUiState> = _uiState.asStateFlow()
    
    init {
        loadLatestLoanWithHistory()
    }
    
    /**
     * Load loan terakhir dari customer dan history-nya.
     */
    fun loadLatestLoanWithHistory() {
        viewModelScope.launch {
            // First, get all my applications
            repository.getMyApplications().collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        val loans = result.data ?: emptyList()
                        // Get the latest loan (first in list since sorted by submittedAt desc)
                        val latestLoan = loans.firstOrNull()
                        
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                loans = loans,
                                latestLoan = latestLoan
                            )
                        }
                        
                        // If there's a latest loan, load its history
                        if (latestLoan != null) {
                            loadHistory(latestLoan.id)
                        }
                    }
                    is Resource.Error -> {
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
    
    /**
     * Load history untuk specific loan application.
     */
    fun loadHistory(loanId: Long) {
        viewModelScope.launch {
            repository.getApplicationHistory(loanId).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoadingHistory = true) }
                    }
                    is Resource.Success -> {
                        _uiState.update { 
                            it.copy(
                                isLoadingHistory = false,
                                history = result.data ?: emptyList()
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoadingHistory = false,
                                error = result.message
                            )
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Select different loan to view its history.
     */
    fun selectLoan(loan: LoanApplicationResponse) {
        _uiState.update { it.copy(latestLoan = loan, history = emptyList()) }
        loadHistory(loan.id)
    }
    
    /**
     * Retry loading data.
     */
    fun retry() {
        _uiState.update { it.copy(error = null) }
        loadLatestLoanWithHistory()
    }
    
    /**
     * Clear error.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
