package com.example.loanova_android.domain.repository

import com.example.loanova_android.core.common.Resource
import com.example.loanova_android.domain.model.Notification
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface untuk Notification.
 */
interface INotificationRepository {
    
    /**
     * Get daftar notifikasi user yang sedang login.
     */
    fun getMyNotifications(): Flow<Resource<List<Notification>>>
    
    /**
     * Tandai notifikasi spesifik sebagai sudah dibaca.
     */
    fun markAsRead(notificationId: Long): Flow<Resource<Unit>>
    
    /**
     * Tandai semua notifikasi sebagai sudah dibaca.
     */
    fun markAllAsRead(): Flow<Resource<Unit>>
}
