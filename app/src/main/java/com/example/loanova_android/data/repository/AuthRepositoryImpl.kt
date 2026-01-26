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
    private val tokenManager: com.example.loanova_android.data.local.TokenManager // Session Manager
) : IAuthRepository {

    /**
     * Implementasi login yang memanggil remote API.
     * 
     * FLOW EKSEKUSI:
     * 1. Buat LoginRequest DTO dari parameter
     * 2. Panggil remoteDataSource untuk network call
     * 3. Parse response dan handle success/error
     * 4. Map DTO ke Domain Model (User)
     * 5. Wrap hasil dalam Result
     * 
     * ERROR HANDLING STRATEGY:
     * - HTTP Success + body.success=true -> Result.success
     * - HTTP Success + body.success=false -> Result.failure dengan message dari API
     * - HTTP Error (4xx, 5xx) -> Parse error body, extract message
     * - Network/Exception -> Wrap exception dalam Result.failure
     * 
     * @param username Username dari UI
     * @param password Password dari UI
     * @param fcmToken FCM Token (Optional)
     * @return Result<User> - Success dengan User domain model, atau Failure dengan Exception
     */
    override fun login(username: String, password: String, fcmToken: String?): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            val response = remoteDataSource.login(LoginRequest(username, password, fcmToken))
            val body = response.body()

            if (response.isSuccessful && body?.success == true && body.data != null) {
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
                // Save session
                if (body.data.accessToken != null) {
                    tokenManager.saveSession(
                        body.data.accessToken, 
                        body.data.refreshToken ?: "",
                        body.data.username ?: username
                    )
                }
            } else {
                val errorMessage = body?.message ?: response.message()
                emit(Resource.Error(errorMessage))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Unknown Network Error"))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Implementasi Logout.
     * 
     * LOGIC:
     * 1. Ambil AccessToken & RefreshToken dari local storage (TokenManager).
     * 2. Kalau token ada, panggil API Logout agar backend blacklist token tsb.
     * 3. APAPUN HASILNYA (Sukses/Gagal/Error Network), kita harus hapus sesi lokal.
     *    Alasannya: Kalau user klik logout, mereka berharap keluar dari app.
     *    Jika API fail (misal offline), user tetap harus bisa logout secara lokal.
     * 
     * @return Flow<Resource<Boolean>> - True jika proses selesai
     */
    override fun logout(): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            val accessToken = tokenManager.getAccessToken() ?: ""
            val refreshToken = tokenManager.getRefreshToken() ?: ""
            
            if (accessToken.isNotEmpty() && refreshToken.isNotEmpty()) {
                val response = remoteDataSource.logout(accessToken, refreshToken)
                if (response.isSuccessful) {
                     // Skenario Ideal: Server sukses blacklist token -> Hapus lokal
                     tokenManager.clearSession()
                     emit(Resource.Success(true))
                } else {
                     // Skenario Server Error (misal 500): Tetap hapus lokal demi keamanan/UX
                     tokenManager.clearSession()
                     emit(Resource.Error(response.message()))
                }
            } else {
                 // Token sudah tidak ada (mungkin sudah terhapus): Anggap sukses logout
                 tokenManager.clearSession()
                 emit(Resource.Success(true))
            }
        } catch (e: Exception) {
            // Skenario Network Error (Offline): Tetap paksa logout lokal
            tokenManager.clearSession()
            emit(Resource.Error(e.message ?: "Logout error"))
        }
    }.flowOn(Dispatchers.IO)
}
