package com.example.loanova_android.data.repository

import android.util.Log
import com.example.loanova_android.core.common.Resource
import com.example.loanova_android.data.local.dao.NotificationDao
import com.example.loanova_android.data.mapper.NotificationMapper
import com.example.loanova_android.data.remote.datasource.NotificationRemoteDataSource
import com.example.loanova_android.domain.model.Notification
import com.example.loanova_android.domain.repository.INotificationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation dari INotificationRepository dengan Offline-First architecture.
 * 
 * Flow:
 * 1. Emit data dari local database (cache) terlebih dahulu
 * 2. Fetch data dari server
 * 3. Simpan data baru ke local database
 * 4. Emit data terbaru dari local database
 * 
 * Keuntungan:
 * - User bisa melihat notifikasi meski offline
 * - UX lebih responsif karena tidak menunggu network
 * - Data tetap konsisten dengan server saat online
 */
@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val remoteDataSource: NotificationRemoteDataSource,
    private val notificationDao: NotificationDao
) : INotificationRepository {
    
    companion object {
        private const val TAG = "NotificationRepo"
        // Cache valid for 30 days
        private const val CACHE_VALIDITY_MS = 30L * 24 * 60 * 60 * 1000
    }
    
    /**
     * Get notifications dengan offline-first strategy.
     * 
     * Strategy:
     * 1. Tampilkan data lokal dulu (jika ada)
     * 2. Fetch dari server di background
     * 3. Update local database
     * 4. Emit data baru dari local
     */
    override fun getMyNotifications(): Flow<Resource<List<Notification>>> = flow {
        emit(Resource.Loading())
        
        // Step 1: Emit cached data first (if available)
        val cachedData = notificationDao.getAllNotifications().firstOrNull()
        if (!cachedData.isNullOrEmpty()) {
            Log.d(TAG, "Emitting ${cachedData.size} notifications from cache")
            val cachedNotifications = NotificationMapper.entityToDomainList(cachedData)
            emit(Resource.Success(cachedNotifications, isFromCache = true))
        }
        
        // Step 2: Fetch from server
        try {
            val response = remoteDataSource.getMyNotifications()
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true && apiResponse.data != null) {
                    // Step 3: Save to local database
                    val entities = NotificationMapper.toEntityList(apiResponse.data)
                    notificationDao.insertAll(entities)
                    Log.d(TAG, "Saved ${entities.size} notifications to local database")
                    
                    // Clean old notifications (older than 30 days)
                    val thirtyDaysAgo = System.currentTimeMillis() - CACHE_VALIDITY_MS
                    notificationDao.deleteOldNotifications(thirtyDaysAgo)
                    
                    // Step 4: Emit fresh data from server
                    val notifications = NotificationMapper.toDomainList(apiResponse.data)
                    emit(Resource.Success(notifications, isFromCache = false))
                } else {
                    // API returned error, but we already emitted cache data
                    if (cachedData.isNullOrEmpty()) {
                        emit(Resource.Error(apiResponse?.message ?: "Gagal mengambil notifikasi"))
                    }
                }
            } else {
                val errorMessage = when (response.code()) {
                    401 -> "Sesi Anda telah berakhir. Silakan login kembali."
                    403 -> "Anda tidak memiliki akses ke fitur ini."
                    500 -> "Terjadi kesalahan pada server."
                    else -> "Gagal mengambil notifikasi (${response.code()})"
                }
                // Only emit error if no cached data
                if (cachedData.isNullOrEmpty()) {
                    emit(Resource.Error(errorMessage))
                } else {
                    Log.w(TAG, "Server error but using cached data: $errorMessage")
                }
            }
        } catch (e: java.net.UnknownHostException) {
            Log.w(TAG, "No internet connection, using cached data")
            if (cachedData.isNullOrEmpty()) {
                emit(Resource.Error("Tidak ada koneksi internet. Periksa jaringan Anda."))
            }
            // Else: We already emitted cached data, user can still use app
        } catch (e: java.net.SocketTimeoutException) {
            Log.w(TAG, "Connection timeout, using cached data")
            if (cachedData.isNullOrEmpty()) {
                emit(Resource.Error("Koneksi timeout. Silakan coba lagi."))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error: ${e.message}", e)
            if (cachedData.isNullOrEmpty()) {
                emit(Resource.Error(e.message ?: "Terjadi kesalahan tidak terduga"))
            }
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Observe notifications sebagai Flow (untuk real-time updates dari local DB)
     */
    fun observeNotifications(): Flow<List<Notification>> {
        return notificationDao.getAllNotifications()
            .map { entities -> NotificationMapper.entityToDomainList(entities) }
            .flowOn(Dispatchers.IO)
    }
    
    /**
     * Observe unread count
     */
    fun observeUnreadCount(): Flow<Int> {
        return notificationDao.getUnreadCount()
            .flowOn(Dispatchers.IO)
    }
    
    override fun markAsRead(notificationId: Long): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        
        try {
            // Step 1: Optimistically update local first (instant UI feedback)
            notificationDao.markAsRead(notificationId)
            Log.d(TAG, "Marked notification $notificationId as read locally")
            
            // Step 2: Sync with server
            val response = remoteDataSource.markAsRead(notificationId)
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    emit(Resource.Success(Unit))
                } else {
                    // Server failed, but local is already updated
                    // User won't notice, will sync on next fetch
                    Log.w(TAG, "Server mark as read failed: ${apiResponse?.message}")
                    emit(Resource.Success(Unit)) // Still success for UX
                }
            } else {
                Log.w(TAG, "Server mark as read failed: ${response.code()}")
                emit(Resource.Success(Unit)) // Still success for UX (optimistic update)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error marking as read: ${e.message}", e)
            // Even if server fails, local is updated
            emit(Resource.Success(Unit)) // Optimistic update
        }
    }.flowOn(Dispatchers.IO)
    
    override fun markAllAsRead(): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        
        try {
            // Step 1: Optimistically update local first
            notificationDao.markAllAsRead()
            Log.d(TAG, "Marked all notifications as read locally")
            
            // Step 2: Sync with server
            val response = remoteDataSource.markAllAsRead()
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    emit(Resource.Success(Unit))
                } else {
                    Log.w(TAG, "Server mark all as read failed: ${apiResponse?.message}")
                    emit(Resource.Success(Unit)) // Still success for UX
                }
            } else {
                Log.w(TAG, "Server mark all as read failed: ${response.code()}")
                emit(Resource.Success(Unit)) // Still success for UX
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error marking all as read: ${e.message}", e)
            emit(Resource.Success(Unit)) // Optimistic update
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Clear all local notification cache (e.g., on logout)
     */
    suspend fun clearCache() {
        withContext(Dispatchers.IO) {
            notificationDao.deleteAll()
            Log.d(TAG, "Cleared notification cache")
        }
    }
}
