package com.example.loanova_android.data.model.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO untuk request reset password.
 * Dikirim ke endpoint POST /api/auth/reset-password
 */
data class ResetPasswordRequest(
    @SerializedName("token")
    val token: String,
    @SerializedName("newPassword")
    val newPassword: String
)
