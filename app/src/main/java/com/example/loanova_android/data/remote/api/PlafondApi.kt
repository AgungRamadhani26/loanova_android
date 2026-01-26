package com.example.loanova_android.data.remote.api

import com.example.loanova_android.core.base.ApiResponse
import com.example.loanova_android.data.model.dto.PlafondResponse
import retrofit2.Response
import retrofit2.http.GET

interface PlafondApi {
    @GET("api/plafonds/public")
    suspend fun getPublicPlafonds(): Response<ApiResponse<List<PlafondResponse>>>
}
