package com.example.loanova_android.data.model.dto

import com.google.gson.annotations.SerializedName

data class ChangePasswordRequest(
    @SerializedName("currentPassword")
    val currentPassword: String,
    
    @SerializedName("newPassword")
    val newPassword: String
)
