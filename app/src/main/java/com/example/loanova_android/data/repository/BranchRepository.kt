package com.example.loanova_android.data.repository

import com.example.loanova_android.core.common.Resource
import com.example.loanova_android.data.local.dao.BranchDao
import com.example.loanova_android.data.mapper.DataMappers
import com.example.loanova_android.data.model.dto.BranchResponse
import com.example.loanova_android.data.remote.api.BranchApi
import com.example.loanova_android.domain.repository.IBranchRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Repository implementation untuk Branch operations.
 * Menggunakan strategi offline-first:
 * 1. Tampilkan cache local jika ada
 * 2. Sync dengan network
 * 3. Update cache dan emit dari cache (single source of truth)
 */
class BranchRepository @Inject constructor(
    private val branchApi: BranchApi,
    private val branchDao: BranchDao
) : IBranchRepository {
    
    /**
     * Get All Branches - OFFLINE-FIRST
     * Branch list jarang berubah, cocok untuk di-cache.
     */
    override suspend fun getAllBranches(): Flow<Resource<List<BranchResponse>>> = flow {
        emit(Resource.Loading())
        
        // 1. Check Local Cache
        var localData: List<com.example.loanova_android.data.local.entity.BranchEntity>? = null
        try {
            localData = branchDao.getAllBranches().firstOrNull()
            if (!localData.isNullOrEmpty()) {
                val cachedResponse = localData.map { DataMappers.mapBranchEntityToResponse(it) }
                emit(Resource.Success(cachedResponse, isFromCache = true))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        // 2. Network Sync
        try {
            val response = branchApi.getAllBranches()
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    val networkData = body.data ?: emptyList()
                    
                    // Update cache: clear and insert fresh data
                    branchDao.deleteAll()
                    val entities = networkData.map { DataMappers.mapBranchResponseToEntity(it) }
                    branchDao.insertAll(entities)
                    
                    // Emit from DB as single source of truth
                    emitAll(branchDao.getAllBranches().map { list ->
                        Resource.Success(list.map { DataMappers.mapBranchEntityToResponse(it) })
                    })
                } else {
                    // API error but have cache -> continue showing cache
                    if (!localData.isNullOrEmpty()) {
                        emitAll(branchDao.getAllBranches().map { list ->
                            Resource.Success(list.map { DataMappers.mapBranchEntityToResponse(it) }, isFromCache = true)
                        })
                    } else {
                        emit(Resource.Error(body?.message ?: "Gagal mengambil data cabang"))
                    }
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
                
                // HTTP error but have cache -> continue showing cache
                if (!localData.isNullOrEmpty()) {
                    emitAll(branchDao.getAllBranches().map { list ->
                        Resource.Success(list.map { DataMappers.mapBranchEntityToResponse(it) }, isFromCache = true)
                    })
                } else {
                    emit(Resource.Error(errorMessage))
                }
            }
        } catch (e: Exception) {
            // Network exception but have cache -> continue showing cache
            if (!localData.isNullOrEmpty()) {
                emitAll(branchDao.getAllBranches().map { list ->
                    Resource.Success(list.map { DataMappers.mapBranchEntityToResponse(it) }, isFromCache = true)
                })
            } else {
                emit(Resource.Error(e.message ?: "Terjadi kesalahan jaringan"))
            }
        }
    }.flowOn(Dispatchers.IO)
}
