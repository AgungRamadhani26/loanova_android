package com.example.loanova_android.domain.model

data class User(
    val username: String,
    val roles: List<String>,
    val permissions: List<String>,
    val accessToken: String,
    val refreshToken: String
)
