package com.example.loanova_android.data.remote.api

// ============================================================================
// LAYER: Data Layer (Remote)
// PATTERN: Retrofit API Interface
// RESPONSIBILITY: Definisi endpoint HTTP untuk komunikasi dengan backend
// ============================================================================

import com.example.loanova_android.core.base.ApiResponse
import com.example.loanova_android.data.model.dto.LoginResponse
import com.example.loanova_android.data.model.dto.LoginRequest
import com.example.loanova_android.data.model.dto.RefreshTokenRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * AuthApi - Interface Retrofit untuk endpoint autentikasi.
 * 
 * APA ITU RETROFIT INTERFACE?
 * - Retrofit menggunakan interface untuk mendefinisikan API endpoints
 * - Anotasi (@POST, @GET, dll) menentukan HTTP method
 * - Parameter dengan @Body/@Query/@Path menentukan cara data dikirim
 * - Retrofit akan generate implementasi pada runtime
 * 
 * KENAPA INTERFACE?
 * - Declarative: Hanya define "apa", bukan "bagaimana"
 * - Retrofit handle HTTP details (serialization, headers, dll)
 * - Easy to read dan maintain
 * - Easy to mock untuk testing
 * 
 * KONVERSI JSON:
 * - Request: LoginRequest -> JSON (via GsonConverterFactory)
 * - Response: JSON -> ApiResponse<Data> (via GsonConverterFactory)
 */
interface AuthApi {
    
    /**
     * Endpoint untuk login user.
     * 
     * HTTP Details:
     * - Method: POST
     * - Path: api/auth/login
     * - Full URL: BASE_URL + "api/auth/login" (BASE_URL di NetworkModule)
     * - Content-Type: application/json (default Retrofit dengan Gson)
     * 
     * @param request LoginRequest DTO yang berisi username & password
     *                @Body annotation: Object akan di-serialize ke JSON dan dikirim di request body
     * 
     * @return Response<ApiResponse<LoginResponse>>
     *         - Response wrapper dari Retrofit untuk akses HTTP metadata
     *         - .isSuccessful: true jika HTTP 2xx
     *         - .body(): ApiResponse jika sukses
     *         - .errorBody(): Error body jika HTTP 4xx/5xx
     *         - .code(): HTTP status code
     * 
     * KENAPA SUSPEND FUNCTION?
     * - Retrofit 2.6+ support suspend functions
     * - Automatically execute in IO dispatcher
     * - No need for enqueue/await pattern
     */
    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<ApiResponse<LoginResponse>>

    /**
     * Endpoint untuk Logout.
     * 
     * METHOD: POST /api/auth/logout
     * HEADER: Authorization: Bearer <token>
     * BODY: { "refreshToken": "..." }
     * 
     * KETENTUAN:
     * - Wajib kirim Header Authorization (akses endpoint ini restricted)
     * - Wajib kirim RefreshToken di body agar backend bisa blacklist
     */
    @POST("api/auth/logout")
    suspend fun logout(
        @retrofit2.http.Header("Authorization") token: String,
        @Body request: RefreshTokenRequest
    ): Response<ApiResponse<Void>>
}
