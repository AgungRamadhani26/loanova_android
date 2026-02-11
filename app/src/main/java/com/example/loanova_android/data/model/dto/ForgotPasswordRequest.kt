package com.example.loanova_android.data.model.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO untuk request forgot password.
 * Dikirim ke endpoint POST /api/auth/forgot-password
 */
data class ForgotPasswordRequest(
    @SerializedName("email")
    val email: String
)
