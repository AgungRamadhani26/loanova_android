package com.example.loanova_android.domain.repository

import com.example.loanova_android.core.common.Resource
import com.example.loanova_android.data.model.dto.BranchResponse
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface untuk Branch operations.
 */
interface IBranchRepository {
    suspend fun getAllBranches(): Flow<Resource<List<BranchResponse>>>
}
