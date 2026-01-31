package com.example.loanova_android.data.remote.api

import com.example.loanova_android.core.base.ApiResponse
import com.example.loanova_android.data.model.dto.BranchResponse
import retrofit2.Response
import retrofit2.http.GET

/**
 * API interface untuk Branch endpoints.
 */
interface BranchApi {
    @GET("api/branches")
    suspend fun getAllBranches(): Response<ApiResponse<List<BranchResponse>>>
}
