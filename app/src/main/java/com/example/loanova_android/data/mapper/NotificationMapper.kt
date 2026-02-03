package com.example.loanova_android.data.mapper

import com.example.loanova_android.data.model.dto.NotificationResponse
import com.example.loanova_android.domain.model.Notification
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * Mapper untuk mengkonversi NotificationResponse DTO ke Notification domain model.
 */
object NotificationMapper {
    
    private val formatters = listOf(
        DateTimeFormatter.ISO_LOCAL_DATE_TIME,
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    )
    
    fun toDomain(response: NotificationResponse): Notification {
        return Notification(
            id = response.id,
            title = response.title,
            message = response.message,
            isRead = response.isRead,
            createdAt = parseDateTime(response.createdAt)
        )
    }
    
    fun toDomainList(responses: List<NotificationResponse>): List<Notification> {
        return responses.map { toDomain(it) }
    }
    
    private fun parseDateTime(dateTimeString: String): LocalDateTime {
        // Remove 'Z' suffix if present and try multiple formats
        val cleanedString = dateTimeString.replace("Z", "")
        
        for (formatter in formatters) {
            try {
                return LocalDateTime.parse(cleanedString, formatter)
            } catch (e: DateTimeParseException) {
                // Try next formatter
            }
        }
        
        // Fallback to now if parsing fails
        return LocalDateTime.now()
    }
}
