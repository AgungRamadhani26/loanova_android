package com.example.loanova_android.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity untuk menyimpan Notification secara lokal.
 * Digunakan untuk offline-first architecture.
 */
@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey
    val id: Long,
    val title: String,
    val message: String,
    val loanApplicationId: Long? = null,
    val isRead: Boolean,
    val createdAt: String, // Store as ISO string for simplicity
    val syncedAt: Long = System.currentTimeMillis() // Track when data was synced
)
