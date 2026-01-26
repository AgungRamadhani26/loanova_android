package com.example.loanova_android.domain.model

import java.math.BigDecimal

data class Plafond(
    val id: Long,
    val name: String,
    val description: String,
    val maxAmount: BigDecimal,
    val interestRate: BigDecimal,
    val tenorMin: Int,
    val tenorMax: Int
)
