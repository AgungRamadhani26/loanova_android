package com.example.loanova_android.data.repository

// ============================================================================
// LAYER: Data Layer
// PATTERN: Repository Implementation - Clean Architecture
// RESPONSIBILITY: Koordinasi data sources dan mapping DTO ke Domain Model
// ============================================================================

import com.example.loanova_android.data.model.dto.LoginRequest
import com.example.loanova_android.data.model.dto.LoginResponse
import com.example.loanova_android.data.remote.datasource.AuthRemoteDataSource
import com.example.loanova_android.domain.model.User
import com.example.loanova_android.domain.repository.IAuthRepository
import com.google.gson.Gson
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
    private val gson: Gson // Untuk deserialize error body
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
    override suspend fun login(username: String, password: String, fcmToken: String?): Result<User> {
        return try {
            // STEP 1 & 2: Buat request DTO dan panggil DataSource
            // LoginRequest adalah DTO yang akan di-serialize ke JSON
            val response = remoteDataSource.login(LoginRequest(username, password, fcmToken))
            
            // STEP 3: Handle response berdasarkan HTTP status
            if (response.isSuccessful) {
                // HTTP 2xx - Response sukses secara network
                val body = response.body()
                
                // Cek apakah body ada dan success flag = true
                if (body != null && body.success && body.data != null) {
                    // STEP 4: Mapping DTO -> Domain Model
                    // LoginResponseData (DTO) -> User (Domain)
                    // Perhatikan null safety dengan Elvis operator (?:)
                    Result.success(
                        User(
                            username = body.data.username ?: username, // Fallback ke input jika null
                            roles = body.data.roles ?: emptyList(),    // Default empty list
                            permissions = body.data.permissions ?: emptyList(),
                            accessToken = body.data.accessToken ?: "",
                            refreshToken = body.data.refreshToken ?: ""
                        )
                    )
                } else {
                    // Body indicate failure (success=false atau body null)
                    // Ini bisa terjadi jika API design return 200 tapi dengan error message
                    Result.failure(Exception(body?.message ?: "Gagal login"))
                }
            } else {
                // HTTP 4xx/5xx - Error response
                // Parse error body untuk mendapatkan message yang lebih informatif
                val errorBody = response.errorBody()?.string()
                val errorResponse = gson.fromJson(errorBody, LoginResponse::class.java)
                
                // Extract error message dengan priority:
                // 1. Validation errors (field-level)
                // 2. General error message
                // 3. Default fallback message
                val errorMessage = when {
                    // Jika ada validation errors, gabungkan semua
                    errorResponse?.data?.errors != null -> {
                        errorResponse.data.errors.values.joinToString(", ")
                    }
                    // Jika ada message umum
                    errorResponse?.message != null -> errorResponse.message
                    // Default fallback
                    else -> "Username atau password salah"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            // Catch-all untuk network errors, parsing errors, dll
            // Log error di sini jika menggunakan logging framework
            Result.failure(e)
        }
    }
}
