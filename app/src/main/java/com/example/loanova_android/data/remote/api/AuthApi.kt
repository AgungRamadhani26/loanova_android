package com.example.loanova_android.data.remote.api

// ============================================================================
// LAYER: Data Layer (Remote)
// PATTERN: Retrofit API Interface
// RESPONSIBILITY: Definisi endpoint HTTP untuk komunikasi dengan backend
// ============================================================================

import com.example.loanova_android.data.model.dto.LoginRequest
import com.example.loanova_android.data.model.dto.LoginResponse
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
 * - Response: JSON -> LoginResponse (via GsonConverterFactory)
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
     * @return Response<LoginResponse>
     *         - Response wrapper dari Retrofit untuk akses HTTP metadata
     *         - .isSuccessful: true jika HTTP 2xx
     *         - .body(): LoginResponse jika sukses
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
    ): Response<LoginResponse>
}
