package com.example.loanova_android.data.model.dto

import com.google.gson.annotations.SerializedName

/**
 * Response DTO untuk Notification dari API Backend.
 * Sesuai dengan NotificationResponse.java di backend.
 */
data class NotificationResponse(
    @SerializedName("id")
    val id: Long,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("isRead")
    val isRead: Boolean,
    
    @SerializedName("createdAt")
    val createdAt: String // ISO 8601 format dari backend
)
