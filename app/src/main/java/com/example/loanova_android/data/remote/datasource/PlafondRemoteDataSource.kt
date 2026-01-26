package com.example.loanova_android.data.remote.datasource

import com.example.loanova_android.core.base.ApiResponse
import com.example.loanova_android.data.model.dto.PlafondResponse
import com.example.loanova_android.data.remote.api.PlafondApi
import retrofit2.Response
import javax.inject.Inject

class PlafondRemoteDataSource @Inject constructor(
    private val plafondApi: PlafondApi
) {
    suspend fun getPublicPlafonds(): Response<ApiResponse<List<PlafondResponse>>> {
        return plafondApi.getPublicPlafonds()
    }
}
