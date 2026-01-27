package com.example.loanova_android.data.model.dto

import com.google.gson.annotations.SerializedName

data class RegisterResponse(
    @SerializedName("id")
    val id: Long? = null,
    
    @SerializedName("username")
    val username: String? = null,
    
    @SerializedName("email")
    val email: String? = null,
    
    @SerializedName("roles")
    val roles: List<String>? = null,
    
    @SerializedName("isActive")
    val isActive: Boolean? = null
)
