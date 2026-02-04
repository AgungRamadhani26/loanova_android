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
import com.example.loanova_android.data.model.dto.RegisterRequest
import com.example.loanova_android.data.model.dto.RegisterResponse
import com.example.loanova_android.data.model.dto.ChangePasswordRequest
import com.example.loanova_android.data.model.dto.FirebaseGoogleLoginRequest
import com.example.loanova_android.data.model.dto.ForgotPasswordRequest
import com.example.loanova_android.data.model.dto.ResetPasswordRequest
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
    @POST("api/auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<ApiResponse<RegisterResponse>>

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

    /**
     * Endpoint untuk Refresh Token.
     * Digunakan oleh TokenAuthenticator saat access token expired (401).
     *
     * @return Call<ApiResponse<LoginResponse>> (Synchronous Call untuk Authenticator)
     */
    @POST("api/auth/refresh")
    fun refreshToken(
        @Body request: RefreshTokenRequest
    ): retrofit2.Call<ApiResponse<LoginResponse>>

    @POST("api/auth/change-password")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest
    ): Response<ApiResponse<Void>>

    /**
     * Endpoint untuk Firebase Google Sign-In.
     * 
     * METHOD: POST /api/auth/firebase-google
     * BODY: { "idToken": "...", "fcmToken": "..." }
     * 
     * FLOW:
     * 1. Android Sign-In dengan Google via Firebase Auth
     * 2. Dapatkan Firebase ID Token
     * 3. Kirim ID Token ke backend
     * 4. Backend verifikasi dengan Firebase Admin SDK
     * 5. Backend create/link user & return JWT tokens
     */
    @POST("api/auth/firebase-google")
    suspend fun loginWithFirebaseGoogle(
        @Body request: FirebaseGoogleLoginRequest
    ): Response<ApiResponse<LoginResponse>>

    /**
     * Endpoint untuk Forgot Password (Lupa Kata Sandi).
     * 
     * METHOD: POST /api/auth/forgot-password
     * BODY: { "email": "user@example.com" }
     * 
     * FLOW:
     * 1. User memasukkan email
     * 2. Backend generate reset token dan kirim ke email
     * 3. Response sukses (tidak ada data, hanya message)
     */
    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(
        @Body request: ForgotPasswordRequest
    ): Response<ApiResponse<Void>>

    /**
     * Endpoint untuk Reset Password.
     * 
     * METHOD: POST /api/auth/reset-password
     * BODY: { "token": "uuid-token", "newPassword": "NewPass123!" }
     * 
     * FLOW:
     * 1. User mendapat token dari email (via deep link)
     * 2. User memasukkan password baru
     * 3. Backend validasi token dan update password
     */
    @POST("api/auth/reset-password")
    suspend fun resetPassword(
        @Body request: ResetPasswordRequest
    ): Response<ApiResponse<Void>>
}
