package com.example.loanova_android.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("loanova_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        // Refresh token diperlukan untuk proses logout (revoke token di backend)
        // dan untuk refresh session jika access token expired (future dev)
        private const val KEY_REFRESH_TOKEN = "refresh_token" 
        private const val KEY_USERNAME = "username"
    }

    /**
     * Menyimpan data sesi lengkap setelah login sukses.
     * @param accessToken Token pendek (JWT) untuk akses API
     * @param refreshToken Token panjang untuk perbarui sesi / logout
     * @param username Identitas user yg login
     */
    fun saveSession(accessToken: String, refreshToken: String, username: String) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putString(KEY_USERNAME, username)
            .apply()
    }

    fun getRefreshToken(): String? {
        return prefs.getString(KEY_REFRESH_TOKEN, null)
    }

    fun getAccessToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    fun getUsername(): String? {
        return prefs.getString(KEY_USERNAME, null)
    }

    /**
     * Menghapus semua data sesi dari HP.
     * Dipanggil saat logout atau saat user memaksa keluar.
     * Setelah ini dipanggil, isLoggedIn() akan return false.
     */
    fun clearSession() {
        prefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean {
        return getAccessToken() != null
    }
}
