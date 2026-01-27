package com.example.loanova_android.ui.features.auth.register

data class RegisterUiState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null,
    val fieldErrors: Map<String, String>? = null
)
