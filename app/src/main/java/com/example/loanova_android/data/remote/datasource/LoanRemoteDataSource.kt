package com.example.loanova_android.data.remote.datasource

import com.example.loanova_android.core.base.ApiResponse
import com.example.loanova_android.data.model.dto.ApplicationHistoryResponse
import com.example.loanova_android.data.model.dto.LoanApplicationResponse
import com.example.loanova_android.data.remote.api.LoanApplicationApi
import okhttp3.RequestBody
import retrofit2.Response
import javax.inject.Inject

/**
 * DataSource untuk Loan Application API operations.
 * 
 * Responsibility:
 * - Abstraksi untuk network calls terkait Loan Application
 * - Bisa ditambahkan logging, retry logic, atau caching di sini
 * - Repository hanya depend ke DataSource, bukan langsung ke API
 */
class LoanRemoteDataSource @Inject constructor(
    private val loanApplicationApi: LoanApplicationApi
) {
    /**
     * Submit new loan application.
     */
    suspend fun submitLoanApplication(body: RequestBody): Response<ApiResponse<LoanApplicationResponse>> {
        return loanApplicationApi.submitLoanApplication(body)
    }
    
    /**
     * Get my loan applications (for CUSTOMER).
     */
    suspend fun getMyApplications(): Response<ApiResponse<List<LoanApplicationResponse>>> {
        return loanApplicationApi.getMyApplications()
    }
    
    /**
     * Get loan application detail by ID.
     */
    suspend fun getApplicationDetail(id: Long): Response<ApiResponse<LoanApplicationResponse>> {
        return loanApplicationApi.getApplicationDetail(id)
    }
    
    /**
     * Get loan application history by ID.
     */
    suspend fun getApplicationHistory(id: Long): Response<ApiResponse<List<ApplicationHistoryResponse>>> {
        return loanApplicationApi.getApplicationHistory(id)
    }
}
