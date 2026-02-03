package com.example.loanova_android.domain.model

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Domain model untuk Notification.
 */
data class Notification(
    val id: Long,
    val title: String,
    val message: String,
    val loanApplicationId: Long? = null,
    val isRead: Boolean,
    val createdAt: LocalDateTime
) {
    /**
     * Format tanggal menjadi relative time (misal: "2 jam lalu", "Kemarin")
     */
    fun getRelativeTime(): String {
        val now = LocalDateTime.now()
        val diff = java.time.Duration.between(createdAt, now)
        
        return when {
            diff.toMinutes() < 1 -> "Baru saja"
            diff.toMinutes() < 60 -> "${diff.toMinutes()} menit lalu"
            diff.toHours() < 24 -> "${diff.toHours()} jam lalu"
            diff.toDays() == 1L -> "Kemarin"
            diff.toDays() < 7 -> "${diff.toDays()} hari lalu"
            else -> createdAt.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
        }
    }
    
    /**
     * Format tanggal lengkap untuk detail
     */
    fun getFormattedDateTime(): String {
        return createdAt.format(DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm"))
    }
}
