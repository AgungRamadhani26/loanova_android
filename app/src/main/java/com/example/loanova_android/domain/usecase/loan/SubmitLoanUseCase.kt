package com.example.loanova_android.domain.usecase.loan

import com.example.loanova_android.core.common.Resource
import com.example.loanova_android.data.model.dto.LoanApplicationRequest
import com.example.loanova_android.data.model.dto.LoanApplicationResponse
import com.example.loanova_android.domain.repository.ILoanApplicationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase untuk submit loan application.
 */
class SubmitLoanUseCase @Inject constructor(
    private val loanApplicationRepository: ILoanApplicationRepository
) {
    suspend operator fun invoke(
        request: LoanApplicationRequest
    ): Flow<Resource<LoanApplicationResponse>> {
        return loanApplicationRepository.submitLoanApplication(request)
    }
}
