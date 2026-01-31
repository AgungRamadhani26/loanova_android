package com.example.loanova_android.data.repository

import com.example.loanova_android.core.common.Resource
import com.example.loanova_android.data.model.dto.BranchResponse
import com.example.loanova_android.data.remote.api.BranchApi
import com.example.loanova_android.domain.repository.IBranchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Repository implementation untuk Branch operations.
 */
class BranchRepository @Inject constructor(
    private val branchApi: BranchApi
) : IBranchRepository {
    
    override suspend fun getAllBranches(): Flow<Resource<List<BranchResponse>>> = flow {
        emit(Resource.Loading())
        
        try {
            val response = branchApi.getAllBranches()
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    emit(Resource.Success(body.data ?: emptyList()))
                } else {
                    emit(Resource.Error(body?.message ?: "Gagal mengambil data cabang"))
                }
            } else {
                // Parse error body
                val errorBody = response.errorBody()?.string()
                val errorMessage = try {
                    val gson = com.google.gson.Gson()
                    val errorResponse = gson.fromJson(errorBody, com.example.loanova_android.core.base.ApiResponse::class.java)
                    errorResponse.message
                } catch (e: Exception) {
                    "Gagal mengambil data cabang"
                }
                emit(Resource.Error(errorMessage))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Terjadi kesalahan jaringan"))
        }
    }
}
