package com.example.loanova_android.data.remote.datasource

import com.example.loanova_android.core.base.ApiResponse
import com.example.loanova_android.data.model.dto.NotificationResponse
import com.example.loanova_android.data.remote.api.NotificationApi
import retrofit2.Response
import javax.inject.Inject

/**
 * DataSource untuk Notification API operations.
 * 
 * Responsibility:
 * - Abstraksi untuk network calls terkait Notification
 * - Bisa ditambahkan logging, retry logic, atau caching di sini
 * - Repository hanya depend ke DataSource, bukan langsung ke API
 */
class NotificationRemoteDataSource @Inject constructor(
    private val notificationApi: NotificationApi
) {
    /**
     * Get list of user notifications.
     */
    suspend fun getMyNotifications(): Response<ApiResponse<List<NotificationResponse>>> {
        return notificationApi.getMyNotifications()
    }
    
    /**
     * Mark specific notification as read.
     */
    suspend fun markAsRead(notificationId: Long): Response<ApiResponse<Unit>> {
        return notificationApi.markAsRead(notificationId)
    }
    
    /**
     * Mark all notifications as read.
     */
    suspend fun markAllAsRead(): Response<ApiResponse<Unit>> {
        return notificationApi.markAllAsRead()
    }
}
