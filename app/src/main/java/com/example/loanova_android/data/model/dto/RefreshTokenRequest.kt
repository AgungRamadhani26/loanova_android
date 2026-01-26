package com.example.loanova_android.data.model.dto

import com.google.gson.annotations.SerializedName

/**
 * RefreshTokenRequest - DTO untuk mengirim refresh token ke backend.
 *
 * DIGUNAKAN UNTUK:
 * 1. Endpoint /api/auth/refresh (Untuk memperpanjang sesi tanpa login ulang)
 * 2. Endpoint /api/auth/logout (Untuk mem-blacklist token di server)
 *
 * ALUR LOGOUT:
 * Frontend kirim: Authorization Header (AccessToken) + Body (RefreshToken)
 * Backend: Blacklist AccessToken & RefreshToken tersebut agar tidak bisa dipakai lagi.
 */
data class RefreshTokenRequest(
    @SerializedName("refreshToken")
    val refreshToken: String
)
