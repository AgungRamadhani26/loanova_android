package com.example.loanova_android.data.mapper

import com.example.loanova_android.data.local.entity.NotificationEntity
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
    
    // ==================== DTO to Domain ====================
    
    fun toDomain(response: NotificationResponse): Notification {
        return Notification(
            id = response.id,
            title = response.title,
            message = response.message,
            loanApplicationId = response.loanApplicationId,
            isRead = response.isRead,
            createdAt = parseDateTime(response.createdAt)
        )
    }
    
    fun toDomainList(responses: List<NotificationResponse>): List<Notification> {
        return responses.map { toDomain(it) }
    }
    
    // ==================== DTO to Entity ====================
    
    fun toEntity(response: NotificationResponse): NotificationEntity {
        return NotificationEntity(
            id = response.id,
            title = response.title,
            message = response.message,
            loanApplicationId = response.loanApplicationId,
            isRead = response.isRead,
            createdAt = response.createdAt,
            syncedAt = System.currentTimeMillis()
        )
    }
    
    fun toEntityList(responses: List<NotificationResponse>): List<NotificationEntity> {
        return responses.map { toEntity(it) }
    }
    
    // ==================== Entity to Domain ====================
    
    fun entityToDomain(entity: NotificationEntity): Notification {
        return Notification(
            id = entity.id,
            title = entity.title,
            message = entity.message,
            loanApplicationId = entity.loanApplicationId,
            isRead = entity.isRead,
            createdAt = parseDateTime(entity.createdAt)
        )
    }
    
    fun entityToDomainList(entities: List<NotificationEntity>): List<Notification> {
        return entities.map { entityToDomain(it) }
    }
    
    // ==================== Helper Functions ====================
    
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
