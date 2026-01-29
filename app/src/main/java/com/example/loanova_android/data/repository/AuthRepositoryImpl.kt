package com.example.loanova_android.data.repository

// ============================================================================
// LAYER: Data Layer
// PATTERN: Repository Implementation - Clean Architecture
// RESPONSIBILITY: Koordinasi data sources dan mapping DTO ke Domain Model
// ============================================================================

import com.example.loanova_android.data.model.dto.LoginRequest

import com.example.loanova_android.data.remote.datasource.AuthRemoteDataSource
import com.example.loanova_android.domain.model.User
import com.example.loanova_android.domain.repository.IAuthRepository
import com.google.gson.Gson
import com.example.loanova_android.core.common.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

import com.example.loanova_android.core.base.BaseRepository
import javax.inject.Inject

/**
 * AuthRepositoryImpl - Implementasi konkrit dari IAuthRepository.
 * 
 * PERAN REPOSITORY DALAM CLEAN ARCHITECTURE:
 * - Repository adalah SINGLE SOURCE OF TRUTH untuk data
 * - Mengkoordinasikan data dari berbagai sources (Remote, Local, Cache)
 * - Bertanggung jawab untuk mapping DTO -> Domain Model
 * - Handle error dan wrap dalam Result untuk clean error handling
 * 
 * KENAPA IMPLEMENT INTERFACE?
 * - Mengikuti Dependency Inversion Principle (DIP)
 * - Domain Layer define interface (IAuthRepository)
 * - Data Layer provide implementation (AuthRepositoryImpl)
 * - Memungkinkan mock/fake untuk testing
 * 
 * DEPENDENCIES:
 * @param remoteDataSource DataSource untuk network operations
 * @param gson Untuk parsing error response dari API
 */
class AuthRepositoryImpl @Inject constructor(
    private val remoteDataSource: AuthRemoteDataSource, // Abstraksi untuk network call
    private val gson: Gson, // Untuk deserialize error body
    private val tokenManager: com.example.loanova_android.data.local.TokenManager, // Session Manager
    private val userDao: com.example.loanova_android.data.local.dao.UserDao // To clear data on logout
) : BaseRepository(gson), IAuthRepository {

    override fun login(username: String, password: String, fcmToken: String?): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            val response = remoteDataSource.login(LoginRequest(username, password, fcmToken))
            val body = response.body()

            if (response.isSuccessful && body?.success == true && body.data != null) {
                // Save session
                if (body.data.accessToken != null) {
                    tokenManager.saveSession(
                        body.data.accessToken, 
                        body.data.refreshToken ?: "",
                        body.data.username ?: username
                    )
                }
                
                emit(
                    Resource.Success(
                        User(
                            username = body.data.username ?: username,
                            roles = body.data.roles ?: emptyList(),
                            permissions = body.data.permissions ?: emptyList(),
                            accessToken = body.data.accessToken ?: "",
                            refreshToken = body.data.refreshToken ?: "",
                            fcmToken = fcmToken
                        )
                    )
                )
            } else {
                 // Use centralized error parsing
                 emit(parseError(response))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Unknown Network Error"))
        }
    }.flowOn(Dispatchers.IO)

    override fun logout(): Flow<Resource<Boolean>> = flow {
         // Logout logic stays almost same, no special error parsing needed usually
         // But let's check duplication
        emit(Resource.Loading())
        try {
            val accessToken = tokenManager.getAccessToken() ?: ""
            val refreshToken = tokenManager.getRefreshToken() ?: ""
            
            if (accessToken.isNotEmpty() && refreshToken.isNotEmpty()) {
                val response = remoteDataSource.logout(accessToken, refreshToken)
                tokenManager.clearSession() // Always clear
                userDao.clearProfile() // Clear profile cache
                userDao.clearAll() // Clear other user data
                
                if (response.isSuccessful) {
                     emit(Resource.Success(true))
                } else {
                     emit(Resource.Error(response.message()))
                }
            } else {
                 tokenManager.clearSession()
                 userDao.clearProfile()
                 userDao.clearAll()
                 emit(Resource.Success(true))
            }
        } catch (e: Exception) {
            tokenManager.clearSession()
            userDao.clearProfile()
            userDao.clearAll()
            emit(Resource.Error(e.message ?: "Logout error"))
        }
    }.flowOn(Dispatchers.IO)

    override fun register(username: String, email: String, password: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            val request = com.example.loanova_android.data.model.dto.RegisterRequest(username, email, password)
            val response = remoteDataSource.register(request)
            val body = response.body()

            if (response.isSuccessful && body?.success == true) {
                emit(Resource.Success(true))
            } else {
                emit(parseError(response))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Unknown Network Error"))
        }
    }.flowOn(Dispatchers.IO)
}
