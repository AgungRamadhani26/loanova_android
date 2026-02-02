package com.example.loanova_android.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity untuk menyimpan Application History di local database.
 * Digunakan untuk offline-first support pada fitur Loan History.
 */
@Entity(tableName = "application_history_entity")
data class ApplicationHistoryEntity(
    @PrimaryKey
    val id: Long,
    val loanApplicationId: Long,
    val actionByUserId: Long,
    val actionByUsername: String,
    val actionByRole: String,
    val status: String,
    val comment: String?,
    val createdAt: String,
    
    // Cache metadata
    val lastSyncedAt: Long = System.currentTimeMillis()
)
