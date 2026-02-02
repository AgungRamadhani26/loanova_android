package com.example.loanova_android.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity untuk menyimpan Branch di local database.
 * Digunakan untuk offline-first support pada fitur Branch Selection.
 */
@Entity(tableName = "branch_entity")
data class BranchEntity(
    @PrimaryKey
    val id: Long,
    val branchCode: String,
    val branchName: String,
    val branchAddress: String,
    
    // Cache metadata
    val lastSyncedAt: Long = System.currentTimeMillis()
)
