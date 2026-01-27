package com.example.loanova_android.data.remote

import com.example.loanova_android.data.local.TokenManager
import com.example.loanova_android.data.model.dto.RefreshTokenRequest
import com.example.loanova_android.data.remote.api.AuthApi
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Provider

/**
 * TokenAuthenticator
 *
 * Interceptor khusus dari OkHttp yang otomatis dipanggil ketika server return 401 Unauthorized.
 * Tugasnya adalah:
 * 1. Cek apakah kita punya Refresh Token.
 * 2. Tembak endpoint /api/auth/refresh (secara synchronous).
 * 3. Jika berhasil -> Simpan Access Token baru -> Retry request sebelumnya dengan token baru.
 * 4. Jika gagal -> Logout (Hapus session).
 */
class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    // Gunakan Provider untuk Lazy Injection karena AuthApi butuh OkHttp, dan OkHttp butuh Authenticator (Circular Dependency)
    private val authApiProvider: Provider<AuthApi>
) : Authenticator {

    override fun authenticate(route: okhttp3.Route?, response: Response): Request? {
        // Mencegah loop infinite jika refresh token sendiri yang 401
        if (response.request.header("Authorization") == null) {
            return null
        }
        
        // Ambil refresh token dari local storage
        val refreshToken = tokenManager.getRefreshToken() ?: return null

        // Panggil endpoint refresh token secara synchronous
        // Kita pakai Provider.get() untuk ambil instance AuthApi saat dibutuhkan
        val authApi = authApiProvider.get()
        
        try {
            val refreshResponse = authApi.refreshToken(RefreshTokenRequest(refreshToken)).execute()

            if (refreshResponse.isSuccessful) {
                val body = refreshResponse.body()
                val newAccessToken = body?.data?.accessToken

                if (newAccessToken != null) {
                    // Simpan token baru
                    // Note: Backend mungkin return refresh token baru atau null (jika rotasi dimatikan).
                    // Jika null, tetap pakai refresh token lama.
                    val newRefreshToken = body.data.refreshToken ?: refreshToken
                    val username = body.data.username ?: tokenManager.getUsername() ?: ""
                    
                    tokenManager.saveSession(newAccessToken, newRefreshToken, username)

                    // Retry request asli dengan header baru
                    return response.request.newBuilder()
                        .header("Authorization", "Bearer $newAccessToken")
                        .build()
                }
            } else {
                // Refresh gagal (misal refresh token expired juga)
                tokenManager.clearSession()
            }
        } catch (e: Exception) {
            // Error network dll
            // tokenManager.clearSession() // Opsional: logout user jika error fatal
        }

        return null // Berarti give up, biarkan error 401 naik ke UI
    }
}
