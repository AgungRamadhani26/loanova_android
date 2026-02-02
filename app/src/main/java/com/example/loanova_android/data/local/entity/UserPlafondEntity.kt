package com.example.loanova_android.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity untuk menyimpan User Plafond (Plafond Aktif) di local database.
 * Digunakan untuk offline-first support pada fitur Active Plafond.
 */
@Entity(tableName = "user_plafond_entity")
data class UserPlafondEntity(
    @PrimaryKey
    val id: Long,
    val userId: Long,
    val username: String,
    val plafondId: Long,
    val plafondName: String,
    val maxAmount: String, // BigDecimal stored as String for precision
    val remainingAmount: String, // BigDecimal stored as String for precision
    val isActive: Boolean,
    val assignedAt: String,
    
    // Cache metadata
    val lastSyncedAt: Long = System.currentTimeMillis()
)
