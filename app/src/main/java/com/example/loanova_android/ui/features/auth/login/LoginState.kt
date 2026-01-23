package com.example.loanova_android.ui.features.auth.login

import com.example.loanova_android.domain.model.User

data class LoginUiState(
    val isLoading: Boolean = false,
    val success: User? = null,
    val error: String? = null,
)
