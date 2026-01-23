package com.example.loanova_android.data.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val username: String,
    val accessToken: String,
    val refreshToken: String
)
