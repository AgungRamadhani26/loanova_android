package com.example.loanova_android.domain.repository

import com.example.loanova_android.core.common.Resource
import com.example.loanova_android.data.model.dto.LoanApplicationRequest
import com.example.loanova_android.data.model.dto.LoanApplicationResponse
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface untuk Loan Application operations.
 */
interface ILoanApplicationRepository {
    
    /**
     * Submit loan application dengan semua data dan file.
     */
    suspend fun submitLoanApplication(
        request: LoanApplicationRequest
    ): Flow<Resource<LoanApplicationResponse>>
    
    /**
     * Get my loan applications.
     */
    suspend fun getMyApplications(): Flow<Resource<List<LoanApplicationResponse>>>
    
    /**
     * Get application detail by ID.
     */
    suspend fun getApplicationDetail(id: Long): Flow<Resource<LoanApplicationResponse>>
}
