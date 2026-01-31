package com.example.loanova_android.ui.features.loan

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loanova_android.core.common.Resource
import com.example.loanova_android.data.model.dto.BranchResponse
import com.example.loanova_android.data.model.dto.LoanApplicationRequest
import com.example.loanova_android.data.model.dto.LoanApplicationResponse
import com.example.loanova_android.data.model.dto.UserPlafondResponse
import com.example.loanova_android.domain.model.Plafond
import com.example.loanova_android.domain.usecase.branch.GetBranchesUseCase
import com.example.loanova_android.domain.usecase.loan.SubmitLoanUseCase
import com.example.loanova_android.domain.usecase.plafond.GetActivePlafondUseCase
import com.example.loanova_android.domain.usecase.plafond.GetPublicPlafondsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.math.BigDecimal
import javax.inject.Inject

/**
 * UI State untuk Loan Application Screen.
 */
data class LoanApplicationUiState(
    // Loading states
    val isLoadingBranches: Boolean = false,
    val isLoadingPlafonds: Boolean = false,
    val isSubmitting: Boolean = false,
    
    // Data
    val branches: List<BranchResponse> = emptyList(),
    val plafonds: List<Plafond> = emptyList(),
    val activePlafond: UserPlafondResponse? = null,
    
    // Form inputs
    val selectedBranchId: Long? = null,
    val selectedPlafondId: Long? = null,
    val amount: String = "",
    val tenor: Int = 12,
    val occupation: String = "",
    val companyName: String = "",
    val rekeningNumber: String = "",
    val savingBookCoverUri: Uri? = null,
    val payslipPhotoUri: Uri? = null,
    
    // Slider ranges
    val minAmount: BigDecimal = BigDecimal.ZERO,
    val maxAmount: BigDecimal = BigDecimal.ZERO,
    val minTenor: Int = 6,
    val maxTenor: Int = 60,
    
    // Location
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationError: String? = null,
    
    // Result
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val submitResult: LoanApplicationResponse? = null
)

/**
 * ViewModel untuk Loan Application Screen.
 */
@HiltViewModel
class LoanApplicationViewModel @Inject constructor(
    private val getBranchesUseCase: GetBranchesUseCase,
    private val getPublicPlafondsUseCase: GetPublicPlafondsUseCase,
    private val getActivePlafondUseCase: GetActivePlafondUseCase,
    private val submitLoanUseCase: SubmitLoanUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LoanApplicationUiState())
    val uiState: StateFlow<LoanApplicationUiState> = _uiState.asStateFlow()
    
    // Store public plafonds to lookup details
    private var publicPlafondsCache: List<Plafond> = emptyList()
    
    init {
        loadBranches()
        loadInitialData()
    }
    
    fun loadInitialData() {
        viewModelScope.launch {
            // 1. Fetch Active Plafond
            getActivePlafondUseCase.execute().collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoadingPlafonds = true)
                    }
                    is Resource.Success -> {
                        val activePlafond = result.data
                        if (activePlafond != null) {
                            // 2. Fetch Public Plafonds to get tenor details
                            fetchPlafondDetails(activePlafond)
                        } else {
                            // User has no active plafond. Show error blocking submission.
                            _uiState.value = _uiState.value.copy(
                                isLoadingPlafonds = false,
                                errorMessage = "Anda belum memiliki plafond aktif. Silakan hubungi admin.",
                                // Disable form interaction effectively
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoadingPlafonds = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }
    
    private suspend fun fetchPlafondDetails(activePlafond: com.example.loanova_android.data.model.dto.UserPlafondResponse) {
        getPublicPlafondsUseCase.execute().collect { result ->
            if (result is Resource.Success) {
                publicPlafondsCache = result.data ?: emptyList()
                
                // Find matching public plafond to get tenor bounds
                val detailedPlafond = publicPlafondsCache.find { it.id == activePlafond.plafondId }
                
                if (detailedPlafond != null) {
                    // Update UI State with Active Plafond info
                    _uiState.value = _uiState.value.copy(
                        isLoadingPlafonds = false,
                        activePlafond = activePlafond,
                        selectedPlafondId = activePlafond.plafondId, // Auto-select
                        
                        // Limits based on Active Plafond Remaining Amount and Plafond Type Tenor
                        minAmount = BigDecimal("1000000"), // Default 1 Juta
                        maxAmount = activePlafond.remainingAmount, // Cap at remaining amount
                        minTenor = detailedPlafond.tenorMin,
                        maxTenor = detailedPlafond.tenorMax,
                        
                        // Initial values
                        amount = "1000000",
                        tenor = detailedPlafond.tenorMin
                    )
                } else {
                     _uiState.value = _uiState.value.copy(
                        isLoadingPlafonds = false,
                        errorMessage = "Detail plafond tidak ditemukan"
                    )
                }
            } else if (result is Resource.Error) {
                 _uiState.value = _uiState.value.copy(
                    isLoadingPlafonds = false,
                    errorMessage = "Gagal memuat detail plafond: ${result.message}"
                )
            }
        }
    }
    
    fun loadBranches() {
        viewModelScope.launch {
            getBranchesUseCase().collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoadingBranches = true)
                    }
                    is Resource.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoadingBranches = false,
                            branches = result.data ?: emptyList()
                        )
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoadingBranches = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }
    
    fun selectBranch(branchId: Long) {
        _uiState.value = _uiState.value.copy(selectedBranchId = branchId)
    }
    
    fun updateAmount(amount: String) {
        _uiState.value = _uiState.value.copy(amount = amount)
    }
    
    fun updateAmountFromSlider(value: Float) {
        val state = _uiState.value
        if (state.maxAmount > BigDecimal.ZERO) {
            val range = state.maxAmount.subtract(state.minAmount)
            val calculatedAmount = state.minAmount.add(range.multiply(BigDecimal(value.toDouble())))
            // Round to nearest million
            val roundedAmount = calculatedAmount.divide(BigDecimal(1000000))
                .setScale(0, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal(1000000))
            _uiState.value = _uiState.value.copy(amount = roundedAmount.toPlainString())
        }
    }
    
    fun updateTenor(tenor: Int) {
        _uiState.value = _uiState.value.copy(tenor = tenor)
    }
    
    fun updateOccupation(occupation: String) {
        _uiState.value = _uiState.value.copy(occupation = occupation)
    }
    
    fun updateCompanyName(companyName: String) {
        _uiState.value = _uiState.value.copy(companyName = companyName)
    }
    
    fun updateRekeningNumber(rekeningNumber: String) {
        _uiState.value = _uiState.value.copy(rekeningNumber = rekeningNumber)
    }
    
    fun updateSavingBookCover(uri: Uri?) {
        _uiState.value = _uiState.value.copy(savingBookCoverUri = uri)
    }
    
    fun updatePayslipPhoto(uri: Uri?) {
        _uiState.value = _uiState.value.copy(payslipPhotoUri = uri)
    }
    
    fun updateLocation(latitude: Double, longitude: Double) {
        _uiState.value = _uiState.value.copy(
            latitude = latitude,
            longitude = longitude,
            locationError = null
        )
    }
    
    fun setLocationError(error: String) {
        _uiState.value = _uiState.value.copy(locationError = error)
    }
    
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }
    
    fun submitLoanApplication(
        savingBookCoverFile: File,
        payslipPhotoFile: File
    ) {
        val state = _uiState.value
        
        // Validations
        if (state.activePlafond == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "Plafond aktif tidak ditemukan")
            return
        }
        if (state.selectedBranchId == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "Pilih cabang terlebih dahulu")
            return
        }
        // Plafond ID is auto-selected from active plafond
        if (state.selectedPlafondId == null) {
             _uiState.value = _uiState.value.copy(errorMessage = "Data plafond error")
            return
        }
        
        if (state.amount.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Jumlah pinjaman wajib diisi")
            return
        }
        
        // Validate amount vs remaining limit
        try {
            val amountValue = BigDecimal(state.amount)
            if (amountValue > state.maxAmount) {
                 _uiState.value = _uiState.value.copy(errorMessage = "Jumlah pinjaman melebihi sisa limit plafond")
                 return
            }
             if (amountValue < state.minAmount) {
                 _uiState.value = _uiState.value.copy(errorMessage = "Jumlah pinjaman minimal 1 juta")
                 return
            }
        } catch (e: Exception) {
             _uiState.value = _uiState.value.copy(errorMessage = "Format jumlah pinjaman salah")
             return
        }

        if (state.occupation.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Pekerjaan wajib diisi")
            return
        }
        if (state.rekeningNumber.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Nomor rekening wajib diisi")
            return
        }
        if (state.latitude == null || state.longitude == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "Lokasi belum terdeteksi. Pastikan GPS aktif.")
            return
        }
        
        val request = LoanApplicationRequest(
            branchId = state.selectedBranchId,
            amount = state.amount,
            tenor = state.tenor,
            occupation = state.occupation,
            companyName = state.companyName.ifBlank { null },
            rekeningNumber = state.rekeningNumber,
            latitude = state.latitude,
            longitude = state.longitude,
            savingBookCover = savingBookCoverFile,
            payslipPhoto = payslipPhotoFile
        )

        viewModelScope.launch {
            submitLoanUseCase(request).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.value = _uiState.value.copy(isSubmitting = true)
                    }
                    is Resource.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isSubmitting = false,
                            successMessage = result.message ?: "Pengajuan berhasil disubmit!",
                            submitResult = result.data
                        )
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isSubmitting = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }
}
