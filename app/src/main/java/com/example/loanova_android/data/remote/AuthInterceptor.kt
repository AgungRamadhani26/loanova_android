package com.example.loanova_android.data.remote

import com.example.loanova_android.data.local.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Interceptor untuk menyisipkan Token Otentikasi ke setiap Request HTTP.
 * 
 * Fungsi:
 * 1. Mengambil Access Token dari TokenManager.
 * 2. Jika token ada, tambahkan Header "Authorization: Bearer <token>" ke request.
 * 3. Jika token kosong (misal belum login), biarkan request apa adanya.
 * 
 * Ini berjalan otomatis untuk setiap panggilan API via Retrofit.
 */
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        // Ambil token tersimpan
        val token = tokenManager.getAccessToken()
        
        return if (token != null) {
            // Clone request dan tambahkan header
            val newRequest = request.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
            chain.proceed(newRequest)
        } else {
            // Lanjutkan tanpa header jika tidak ada token
            chain.proceed(request)
        }
    }
}
