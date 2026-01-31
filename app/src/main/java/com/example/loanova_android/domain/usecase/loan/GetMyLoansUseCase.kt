package com.example.loanova_android.domain.usecase.loan

import com.example.loanova_android.core.common.Resource
import com.example.loanova_android.data.model.dto.LoanApplicationResponse
import com.example.loanova_android.domain.repository.ILoanApplicationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase untuk mengambil daftar pengajuan pinjaman saya.
 */
class GetMyLoansUseCase @Inject constructor(
    private val loanApplicationRepository: ILoanApplicationRepository
) {
    suspend operator fun invoke(): Flow<Resource<List<LoanApplicationResponse>>> {
        return loanApplicationRepository.getMyApplications()
    }
}
