package com.example.loanova_android.data.model.dto

import java.math.BigDecimal

data class UserPlafondResponse(
    val id: Long,
    val userId: Long,
    val username: String,
    val plafondId: Long,
    val plafondName: String,
    val maxAmount: BigDecimal,
    val remainingAmount: BigDecimal,
    val isActive: Boolean,
    val assignedAt: String // ISO Date String
)
