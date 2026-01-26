package com.example.loanova_android.data.remote.datasource

// ============================================================================
// LAYER: Data Layer (Remote)
// PATTERN: DataSource - Clean Architecture
// RESPONSIBILITY: Abstraksi untuk operasi network (remote data source)
// ============================================================================

import com.example.loanova_android.core.base.ApiResponse
import com.example.loanova_android.data.model.dto.LoginResponse
import com.example.loanova_android.data.model.dto.LoginRequest
import com.example.loanova_android.data.model.dto.RefreshTokenRequest
import com.example.loanova_android.data.remote.api.AuthApi
import retrofit2.Response
import javax.inject.Inject

/**
 * AuthRemoteDataSource - DataSource untuk operasi remote (API) autentikasi.
 * 
 * APA ITU DATASOURCE?
 * - DataSource adalah abstraksi untuk satu jenis sumber data
 * - RemoteDataSource: Data dari network/API
 * - LocalDataSource: Data dari local database/file
 * - Repository mengkoordinasikan multiple DataSources
 * 
 * KENAPA BUTUH DATASOURCE TERPISAH?
 * 1. SINGLE RESPONSIBILITY: Setiap DataSource handle satu sumber data
 * 2. ABSTRACTION: Repository tidak langsung depend ke API interface
 * 3. FLEXIBILITY: Mudah menambah logic seperti caching, retry, logging
 * 4. TESTABILITY: Bisa mock DataSource untuk testing Repository
 * 
 * FLOW DATA:
 * ```
 * Repository -> RemoteDataSource -> Api (Retrofit) -> Network
 *            -> LocalDataSource -> Dao (Room) -> SQLite
 * ```
 * 
 * DEPENDENCY INJECTION:
 * @param authApi Retrofit interface untuk API calls (di-inject oleh Hilt)
 */
class AuthRemoteDataSource @Inject constructor(
    private val authApi: AuthApi // Retrofit API interface
) {
    /**
     * Melakukan login request ke backend.
     * 
     * FLOW:
     * 1. Terima LoginRequest DTO dari Repository
     * 2. Delegate ke AuthApi untuk actual HTTP call
     * 3. Return Response wrapper ke Repository
     * 
     * KENAPA TIDAK LANGSUNG RETURN User?
     * - DataSource hanya bertanggung jawab untuk I/O operation
     * - Mapping DTO -> Domain Model adalah tugas Repository
     * - Ini mengikuti Single Responsibility Principle
     * 
     * DI SINI BISA DITAMBAHKAN:
     * - Logging request/response
     * - Retry logic
     * - Request modification
     * - Response caching
     * 
     * @param request LoginRequest berisi credentials user
     * @return Response<ApiResponse<LoginResponse>> raw response dari API
     */
    suspend fun login(request: LoginRequest): Response<ApiResponse<LoginResponse>> {
        // Direct delegation ke API - simple proxy
        // Logic tambahan bisa ditambahkan di sini jika diperlukan
        return authApi.login(request)
    }

    /**
     * Mengirim request logout ke API.
     * 
     * DETAIL IMPLEMENTASI:
     * - Menambahkan pretext "Bearer " ke token agar sesuai format Authorization Header standard.
     * - Membungkus refreshToken ke dalam RefreshTokenRequest DTO.
     * 
     * @param token AccessToken (Raw string: "eyJ...")
     * @param refreshToken RefreshToken
     */
    suspend fun logout(token: String, refreshToken: String): Response<ApiResponse<Void>> {
        return authApi.logout(
             token = "Bearer $token",
             request = RefreshTokenRequest(refreshToken)
        )
    }
}
