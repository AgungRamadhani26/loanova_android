package com.example.loanova_android.data.repository

import com.example.loanova_android.core.common.Resource
import com.example.loanova_android.data.remote.datasource.PlafondRemoteDataSource
import com.example.loanova_android.domain.model.Plafond
import com.example.loanova_android.domain.repository.IPlafondRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

import com.example.loanova_android.data.local.dao.PlafondDao
import com.example.loanova_android.data.mapper.DataMappers

class PlafondRepositoryImpl @Inject constructor(
    private val remoteDataSource: PlafondRemoteDataSource,
    private val localDataSource: PlafondDao
) : IPlafondRepository {

    override fun getPublicPlafonds(): Flow<Resource<List<Plafond>>> = flow {
        emit(Resource.Loading())
        
        // 1. Emit Local Data First (Single emission for quick UI show)
        // We collect first emission of DB flow to show immediate cache
        try {
            val localData = localDataSource.getAllPlafonds().firstOrNull()
            if (!localData.isNullOrEmpty()) {
                val domainData = localData.map { DataMappers.mapPlafondEntityToDomain(it) }
                emit(Resource.Success(domainData))
            }
        } catch (e: Exception) {
            // Ignore local read errors, proceed to network
        }

        // 2. Fetch from Network & Sync
        try {
            val response = remoteDataSource.getPublicPlafonds()
            val body = response.body()

            if (response.isSuccessful && body?.success == true && body.data != null) {
                // Map Response -> Entity
                val entities = body.data.map { DataMappers.mapPlafondResponseToEntity(it) }
                
                // Save to DB (Background Sync)
                localDataSource.deleteAll() // Clear old cache
                localDataSource.insertAll(entities)
                
                // Emit Updated Data from DB (Source of Truth)
                localDataSource.getAllPlafonds().collect { newLocalData ->
                     val domainData = newLocalData.map { DataMappers.mapPlafondEntityToDomain(it) }
                     emit(Resource.Success(domainData))
                }
            } else {
                // Network Error
                val errorMessage = body?.message ?: response.message()
                
                // CRITICAL FIX: Only emit Error if we have NO valid data locally.
                // If we already emitted local data, we swallow the error (or use a SideEffect channel)
                // so the UI doesn't flicker to Error screen.
                val currentLocal = localDataSource.getAllPlafonds().firstOrNull()
                if (currentLocal.isNullOrEmpty()) {
                    emit(Resource.Error(errorMessage))
                }
            }
        } catch (e: Exception) {
            // CRITICAL FIX: Same logic for Exceptions (Offline)
            val currentLocal = localDataSource.getAllPlafonds().firstOrNull()
            if (currentLocal.isNullOrEmpty()) {
                emit(Resource.Error(e.localizedMessage ?: "Unknown Network Error"))
            }
        }
    }.flowOn(Dispatchers.IO)
}
