package com.example.loanova_android.data.model.dto

/**
 * DTO untuk request reset password.
 * Dikirim ke endpoint POST /api/auth/reset-password
 */
data class ResetPasswordRequest(
    val token: String,
    val newPassword: String
)
