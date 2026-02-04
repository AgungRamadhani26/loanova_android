package com.example.loanova_android.data.model.dto

/**
 * DTO untuk request forgot password.
 * Dikirim ke endpoint POST /api/auth/forgot-password
 */
data class ForgotPasswordRequest(
    val email: String
)
