package com.example.loanova_android.data.remote

import com.example.loanova_android.data.local.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        // Skip adding token for auth/register endpoints if needed, but it's usually harmless unless server is strict.
        // Better to just add it if we have it, except for refresh token endpoint if it uses different auth.
        // For now, simple logic: if we have token, add it.
        
        val token = tokenManager.getAccessToken()
        
        return if (token != null) {
            val newRequest = request.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
            chain.proceed(newRequest)
        } else {
            chain.proceed(request)
        }
    }
}
