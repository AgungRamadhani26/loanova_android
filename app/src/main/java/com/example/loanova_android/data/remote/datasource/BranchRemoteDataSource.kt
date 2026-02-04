package com.example.loanova_android.data.remote.datasource

import com.example.loanova_android.core.base.ApiResponse
import com.example.loanova_android.data.model.dto.BranchResponse
import com.example.loanova_android.data.remote.api.BranchApi
import retrofit2.Response
import javax.inject.Inject

/**
 * DataSource untuk Branch API operations.
 * 
 * Responsibility:
 * - Abstraksi untuk network calls terkait Branch
 * - Bisa ditambahkan logging, retry logic, atau caching di sini
 * - Repository hanya depend ke DataSource, bukan langsung ke API
 */
class BranchRemoteDataSource @Inject constructor(
    private val branchApi: BranchApi
) {
    /**
     * Get all available branches.
     */
    suspend fun getAllBranches(): Response<ApiResponse<List<BranchResponse>>> {
        return branchApi.getAllBranches()
    }
}
