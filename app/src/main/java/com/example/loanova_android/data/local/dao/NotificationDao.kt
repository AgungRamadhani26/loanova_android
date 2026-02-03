package com.example.loanova_android.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.loanova_android.data.local.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO untuk operasi database Notification.
 * Menyediakan akses offline-first ke data notifikasi.
 */
@Dao
interface NotificationDao {
    
    /**
     * Get semua notifikasi, diurutkan dari terbaru.
     * Menggunakan Flow untuk reactive updates.
     */
    @Query("SELECT * FROM notifications ORDER BY createdAt DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>
    
    /**
     * Get notifikasi berdasarkan ID
     */
    @Query("SELECT * FROM notifications WHERE id = :notificationId")
    suspend fun getNotificationById(notificationId: Long): NotificationEntity?
    
    /**
     * Get jumlah notifikasi yang belum dibaca
     */
    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>
    
    /**
     * Insert atau replace notifikasi (untuk sync dari server)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notifications: List<NotificationEntity>)
    
    /**
     * Insert single notification
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: NotificationEntity)
    
    /**
     * Update notifikasi
     */
    @Update
    suspend fun update(notification: NotificationEntity)
    
    /**
     * Tandai notifikasi sebagai sudah dibaca
     */
    @Query("UPDATE notifications SET isRead = 1 WHERE id = :notificationId")
    suspend fun markAsRead(notificationId: Long)
    
    /**
     * Tandai semua notifikasi sebagai sudah dibaca
     */
    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllAsRead()
    
    /**
     * Hapus semua notifikasi (untuk clear cache atau logout)
     */
    @Query("DELETE FROM notifications")
    suspend fun deleteAll()
    
    /**
     * Hapus notifikasi yang sudah lama (lebih dari 30 hari)
     * syncedAt dalam milliseconds
     */
    @Query("DELETE FROM notifications WHERE syncedAt < :olderThan")
    suspend fun deleteOldNotifications(olderThan: Long)
    
    /**
     * Check apakah ada data lokal
     */
    @Query("SELECT COUNT(*) > 0 FROM notifications")
    suspend fun hasLocalData(): Boolean
}
