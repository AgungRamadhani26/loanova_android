package com.example.loanova_android.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "plafond_entity")
data class PlafondEntity(
    @PrimaryKey
    val id: Long,
    val name: String,
    val description: String,
    val maxAmount: String, // Room doesn't support BigDecimal directly easily without converters, storing as String is safer for precision
    val interestRate: Double, // Double is usually fine for rates, or use String if high precision needed
    val tenorMin: Int,
    val tenorMax: Int
)
