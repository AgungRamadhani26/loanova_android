package com.example.loanova_android.data.model.dto

import java.io.File

/**
 * Data Transfer Object for Completing User Profile.
 * Maps to backend `UserProfileCompleteRequest`.
 */
data class UserProfileCompleteRequest(
    val fullName: String,
    val phoneNumber: String,
    val userAddress: String,
    val nik: String,
    val birthDate: String, // YYYY-MM-DD
    val npwpNumber: String? = null,
    val ktpPhoto: File? = null,
    val profilePhoto: File? = null,
    val npwpPhoto: File? = null
)
