package com.example.loanova_android.data.repository

import com.example.loanova_android.data.local.dao.UserPlafondDao
import com.example.loanova_android.data.mapper.DataMappers
import com.example.loanova_android.data.model.dto.UserPlafondResponse
import com.example.loanova_android.data.remote.datasource.UserPlafondRemoteDataSource
import com.example.loanova_android.domain.repository.IUserProfileRepository
import com.example.loanova_android.domain.repository.IUserPlafondRepository
import com.example.loanova_android.core.common.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

/**
 * Repository implementation untuk User Plafond (Active Plafond).
 * Menggunakan strategi offline-first:
 * 1. Tampilkan cache local jika ada
 * 2. Sync dengan network
 * 3. Update cache dan emit data terbaru
 */
class UserPlafondRepositoryImpl @Inject constructor(
    private val remoteDataSource: UserPlafondRemoteDataSource,
    private val userProfileRepository: IUserProfileRepository,
    private val userPlafondDao: UserPlafondDao
) : IUserPlafondRepository {

    override fun getActivePlafond(): Flow<Result<UserPlafondResponse>> = flow {
        // 1. Get User ID from profile
        val profileResult = userProfileRepository.getMyProfile()
            .filter { it !is Resource.Loading }
            .first()
        
        if (profileResult is Resource.Success && profileResult.data != null) {
            val userId = profileResult.data.userId!!
            
            // 2. Check Local Cache first
            val localData = try {
                userPlafondDao.getActivePlafondByUserIdSync(userId)
            } catch (e: Exception) {
                null
            }
            
            // Emit cached data immediately if available
            if (localData != null) {
                emit(Result.success(DataMappers.mapUserPlafondEntityToResponse(localData)))
            }
            
            // 3. Try Network Sync
            val networkResult = try {
                val response = remoteDataSource.getActiveUserPlafond(userId)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.success && body.data != null) {
                        // Update cache
                        val entity = DataMappers.mapUserPlafondResponseToEntity(body.data)
                        userPlafondDao.deleteByUserId(userId)
                        userPlafondDao.insert(entity)
                        Result.success(body.data)
                    } else {
                        Result.failure<UserPlafondResponse>(Exception(body.message ?: "Data plafond kosong"))
                    }
                } else {
                    Result.failure<UserPlafondResponse>(Exception("Gagal mengambil data plafond: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure<UserPlafondResponse>(e)
            }
            
            // 4. Handle network result
            if (networkResult.isSuccess) {
                // Emit fresh data from network
                emit(networkResult)
            } else if (localData == null) {
                // No cache and network failed - emit error
                emit(networkResult)
            }
            // If network failed but we have cache, we already emitted cache data above
            
        } else if (profileResult is Resource.Error) {
            emit(Result.failure(Exception(profileResult.message ?: "Gagal memuat profil user")))
        } else {
            emit(Result.failure(Exception("Profil user tidak ditemukan")))
        }
    }.flowOn(Dispatchers.IO)
}
