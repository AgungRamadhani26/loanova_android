package com.example.loanova_android.core.base

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: T?,
    @SerializedName("code") val code: Int,
    @SerializedName("timestamp") val timestamp: String?
)
