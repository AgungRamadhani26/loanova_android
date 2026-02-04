package com.example.loanova_android.data.repository

// ============================================================================
// LAYER: Data Layer
// PATTERN: Repository Implementation - Clean Architecture
// RESPONSIBILITY: Koordinasi data sources dan mapping DTO ke Domain Model
// ============================================================================

import com.example.loanova_android.data.model.dto.LoginRequest
import com.example.loanova_android.data.model.dto.ChangePasswordRequest
import com.example.loanova_android.data.model.dto.RegisterRequest
import com.example.loanova_android.data.model.dto.RegisterResponse
import com.example.loanova_android.data.model.dto.FirebaseGoogleLoginRequest
import com.example.loanova_android.data.model.dto.ForgotPasswordRequest
import com.example.loanova_android.data.model.dto.ResetPasswordRequest

import com.example.loanova_android.data.remote.datasource.AuthRemoteDataSource
import com.example.loanova_android.data.remote.datasource.FirebaseAuthDataSource
import com.example.loanova_android.domain.model.User
import com.example.loanova_android.domain.repository.IAuthRepository
import com.google.gson.Gson
import com.example.loanova_android.core.common.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

import com.example.loanova_android.core.base.BaseRepository
import com.example.loanova_android.core.base.ApiResponse
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
    private val firebaseAuthDataSource: FirebaseAuthDataSource, // Firebase Authentication
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
            val request = RegisterRequest(username, email, password)
            val response = remoteDataSource.register(request)
            val body = response.body()

            if (response.isSuccessful && body?.success == true) {
                emit(Resource.Success(true))
            } else {
                emit(parseError<ApiResponse<RegisterResponse>, Boolean>(response))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Unknown Network Error"))
        }
    }.flowOn(Dispatchers.IO)

    override fun changePassword(request: ChangePasswordRequest): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        try {
            // We need to add changePassword to AuthRemoteDataSource as well.
            // For now, let's see if we can access the api service directly or add it to DataSource.
            // Best practice: Add to DataSource first.
            val response = remoteDataSource.changePassword(request)
            if (response.isSuccessful) {
                 emit(Resource.Success("Password berhasil diubah. Silakan login kembali."))
            } else {
                 val errorMsg = parseError<ApiResponse<Void>, Void>(response)
                 if (errorMsg is Resource.Error) {
                      emit(Resource.Error(errorMsg.message ?: "Gagal mengubah password"))
                 } else {
                      emit(Resource.Error("Gagal mengubah password"))
                 }
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Terjadi kesalahan jaringan"))
        }
    }.flowOn(Dispatchers.IO)

    override fun loginWithFirebaseGoogle(idToken: String, fcmToken: String?): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            val request = FirebaseGoogleLoginRequest(idToken, fcmToken)
            val response = remoteDataSource.loginWithFirebaseGoogle(request)
            val body = response.body()

            if (response.isSuccessful && body?.success == true && body.data != null) {
                // Save session - sama seperti login biasa
                if (body.data.accessToken != null) {
                    tokenManager.saveSession(
                        body.data.accessToken,
                        body.data.refreshToken ?: "",
                        body.data.username ?: "Google User"
                    )
                }

                emit(
                    Resource.Success(
                        User(
                            username = body.data.username ?: "Google User",
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
            emit(Resource.Error(e.localizedMessage ?: "Google Sign-In failed"))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Complete Google Sign-In flow:
     * 1. Sign in with Firebase using Google ID Token
     * 2. Get Firebase ID Token
     * 3. Send Firebase ID Token to backend for authentication
     * 
     * This moves Firebase Auth logic from UI layer to Data layer (Clean Architecture)
     */
    override fun signInWithGoogle(googleIdToken: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            // Step 1: Sign in with Firebase using Google credential
            val firebaseAuthResult = firebaseAuthDataSource.signInWithGoogle(googleIdToken)
            
            if (firebaseAuthResult.isFailure) {
                emit(Resource.Error(firebaseAuthResult.exceptionOrNull()?.message ?: "Firebase sign-in failed"))
                return@flow
            }
            
            // Step 2: Get Firebase ID Token
            val firebaseIdTokenResult = firebaseAuthDataSource.getFirebaseIdToken()
            
            if (firebaseIdTokenResult.isFailure || firebaseIdTokenResult.getOrNull() == null) {
                emit(Resource.Error("Failed to get Firebase ID token"))
                return@flow
            }
            
            val firebaseIdToken = firebaseIdTokenResult.getOrNull()!!
            
            // Step 3: Send Firebase ID Token to backend
            val request = FirebaseGoogleLoginRequest(firebaseIdToken, null)
            val response = remoteDataSource.loginWithFirebaseGoogle(request)
            val body = response.body()

            if (response.isSuccessful && body?.success == true && body.data != null) {
                // Save session
                if (body.data.accessToken != null) {
                    tokenManager.saveSession(
                        body.data.accessToken,
                        body.data.refreshToken ?: "",
                        body.data.username ?: "Google User"
                    )
                }

                emit(
                    Resource.Success(
                        User(
                            username = body.data.username ?: "Google User",
                            roles = body.data.roles ?: emptyList(),
                            permissions = body.data.permissions ?: emptyList(),
                            accessToken = body.data.accessToken ?: "",
                            refreshToken = body.data.refreshToken ?: "",
                            fcmToken = null
                        )
                    )
                )
            } else {
                emit(parseError(response))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Google Sign-In failed"))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Request forgot password - mengirim link reset password ke email.
     */
    override fun forgotPassword(email: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        try {
            val request = ForgotPasswordRequest(email)
            val response = remoteDataSource.forgotPassword(request)
            
            if (response.isSuccessful) {
                val message = response.body()?.message ?: "Link reset password telah dikirim ke email Anda"
                emit(Resource.Success(message))
            } else {
                val errorResult = parseError<com.example.loanova_android.core.base.ApiResponse<Void>, String>(response)
                if (errorResult is Resource.Error) {
                    emit(Resource.Error(errorResult.message ?: "Gagal mengirim email reset password"))
                } else {
                    emit(Resource.Error("Gagal mengirim email reset password"))
                }
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Terjadi kesalahan jaringan"))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Reset password dengan token dari email.
     */
    override fun resetPassword(token: String, newPassword: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        try {
            val request = ResetPasswordRequest(token, newPassword)
            val response = remoteDataSource.resetPassword(request)
            
            if (response.isSuccessful) {
                val message = response.body()?.message ?: "Password berhasil diubah"
                emit(Resource.Success(message))
            } else {
                val errorResult = parseError<com.example.loanova_android.core.base.ApiResponse<Void>, String>(response)
                if (errorResult is Resource.Error) {
                    emit(Resource.Error(errorResult.message ?: "Gagal reset password"))
                } else {
                    emit(Resource.Error("Gagal reset password"))
                }
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Terjadi kesalahan jaringan"))
        }
    }.flowOn(Dispatchers.IO)
}
