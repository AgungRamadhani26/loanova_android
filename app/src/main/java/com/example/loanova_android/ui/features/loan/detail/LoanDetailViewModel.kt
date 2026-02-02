package com.example.loanova_android.ui.features.loan.detail

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
 * UI State untuk Loan Detail Screen
 */
data class LoanDetailUiState(
    val isLoading: Boolean = false,
    val loan: LoanApplicationResponse? = null,
    val error: String? = null
)

/**
 * ViewModel untuk menampilkan detail loan application.
 */
@HiltViewModel
class LoanDetailViewModel @Inject constructor(
    private val loanApplicationRepository: ILoanApplicationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoanDetailUiState())
    val uiState: StateFlow<LoanDetailUiState> = _uiState.asStateFlow()
    
    private var currentLoanId: Long = -1L

    /**
     * Load loan detail from API.
     */
    fun loadLoanDetail(id: Long) {
        currentLoanId = id
        viewModelScope.launch {
            loanApplicationRepository.getApplicationDetail(id).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                loan = result.data,
                                error = null
                            ) 
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
     * Retry loading.
     */
    fun retry() {
        if (currentLoanId != -1L) {
            loadLoanDetail(currentLoanId)
        }
    }

    /**
     * Clear error.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
