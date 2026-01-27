package com.example.loanova_android.data.model.dto

import com.google.gson.annotations.SerializedName

data class UserProfileResponse(
    @SerializedName("id")
    val id: Long,
    
    @SerializedName("userId")
    val userId: Long,
    
    @SerializedName("username")
    val username: String,
    
    @SerializedName("fullName")
    val fullName: String,
    
    @SerializedName("phoneNumber")
    val phoneNumber: String,
    
    @SerializedName("userAddress")
    val userAddress: String,
    
    @SerializedName("nik")
    val nik: String,
    
    @SerializedName("birthDate")
    val birthDate: String, // format: "yyyy-MM-dd"
    
    @SerializedName("npwpNumber")
    val npwpNumber: String?,
    
    @SerializedName("ktpPhoto")
    val ktpPhoto: String?,
    
    @SerializedName("profilePhoto")
    val profilePhoto: String?,
    
    @SerializedName("npwpPhoto")
    val npwpPhoto: String?,
    
    @SerializedName("createdAt")
    val createdAt: String?,
    
    @SerializedName("updatedAt")
    val updatedAt: String?
)
