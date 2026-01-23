package com.example.loanova_android.data.model.dto

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("code")
    val code: Int,
    @SerializedName("timestamp")
    val timestamp: String,
    @SerializedName("data")
    val data: LoginData? = null
)

data class LoginData(
    @SerializedName("accessToken")
    val accessToken: String? = null,
    @SerializedName("refreshToken")
    val refreshToken: String? = null,
    @SerializedName("type")
    val type: String? = null,
    @SerializedName("username")
    val username: String? = null,
    @SerializedName("roles")
    val roles: List<String>? = null,
    @SerializedName("permissions")
    val permissions: List<String>? = null,
    @SerializedName("errors")
    val errors: Map<String, String>? = null
)
