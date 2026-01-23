package com.example.loanova_android.ui.features.auth.login

data class LoginResult(val username: String)

data class LoginUiState(
    val isLoading: Boolean = false,
    val success: LoginResult? = null,
    val error: String? = null,
)
