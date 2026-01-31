package com.example.loanova_android.domain.usecase.branch

import com.example.loanova_android.core.common.Resource
import com.example.loanova_android.data.model.dto.BranchResponse
import com.example.loanova_android.domain.repository.IBranchRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase untuk mengambil daftar branch.
 */
class GetBranchesUseCase @Inject constructor(
    private val branchRepository: IBranchRepository
) {
    suspend operator fun invoke(): Flow<Resource<List<BranchResponse>>> {
        return branchRepository.getAllBranches()
    }
}
