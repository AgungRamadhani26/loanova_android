package com.example.loanova_android.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "user_profile_entity")
data class UserProfileEntity(
    @PrimaryKey
    val userId: Long, // Use userId as Primary Key since it's unique per user
    val id: Long, // Profile ID
    val username: String,
    val fullName: String,
    val phoneNumber: String,
    val userAddress: String,
    val nik: String,
    val birthDate: String,
    val npwpNumber: String?, // Nullable
    val ktpPhoto: String?,   // Nullable (URL)
    val profilePhoto: String?, // Nullable (URL)
    val npwpPhoto: String?,  // Nullable (URL)
    val createdAt: String?,
    val updatedAt: String?
)
