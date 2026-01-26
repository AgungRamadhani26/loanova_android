package com.example.loanova_android.data.model.dto

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("username")
    val username: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("fcmToken")
    val fcmToken: String? = null
)
