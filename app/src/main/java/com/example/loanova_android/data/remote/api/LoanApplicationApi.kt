package com.example.loanova_android.data.remote.api

import com.example.loanova_android.core.base.ApiResponse
import com.example.loanova_android.data.model.dto.LoanApplicationResponse
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

/**
 * API interface untuk Loan Application endpoints.
 */
interface LoanApplicationApi {
    
    /**
     * Submit new loan application.
     * Menggunakan multipart/form-data karena ada file upload.
     */
    @POST("api/loan-applications")
    suspend fun submitLoanApplication(
        @Body body: RequestBody
    ): Response<ApiResponse<LoanApplicationResponse>>
    
    /**
     * Get my loan applications (for CUSTOMER).
     */
    @GET("api/loan-applications/my")
    suspend fun getMyApplications(): Response<ApiResponse<List<LoanApplicationResponse>>>
    
    /**
     * Get loan application detail by ID.
     */
    @GET("api/loan-applications/{id}")
    suspend fun getApplicationDetail(
        @Path("id") id: Long
    ): Response<ApiResponse<LoanApplicationResponse>>
}
