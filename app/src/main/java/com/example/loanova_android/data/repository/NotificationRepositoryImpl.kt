package com.example.loanova_android.data.repository

import com.example.loanova_android.core.common.Resource
import com.example.loanova_android.data.mapper.NotificationMapper
import com.example.loanova_android.data.remote.api.NotificationApi
import com.example.loanova_android.domain.model.Notification
import com.example.loanova_android.domain.repository.INotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation dari INotificationRepository.
 * Menghandle fetching notifikasi dari API backend.
 */
@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val notificationApi: NotificationApi
) : INotificationRepository {
    
    override fun getMyNotifications(): Flow<Resource<List<Notification>>> = flow {
        emit(Resource.Loading())
        
        try {
            val response = notificationApi.getMyNotifications()
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true && apiResponse.data != null) {
                    val notifications = NotificationMapper.toDomainList(apiResponse.data)
                    emit(Resource.Success(notifications))
                } else {
                    emit(Resource.Error(apiResponse?.message ?: "Gagal mengambil notifikasi"))
                }
            } else {
                val errorMessage = when (response.code()) {
                    401 -> "Sesi Anda telah berakhir. Silakan login kembali."
                    403 -> "Anda tidak memiliki akses ke fitur ini."
                    500 -> "Terjadi kesalahan pada server."
                    else -> "Gagal mengambil notifikasi (${response.code()})"
                }
                emit(Resource.Error(errorMessage))
            }
        } catch (e: java.net.UnknownHostException) {
            emit(Resource.Error("Tidak ada koneksi internet. Periksa jaringan Anda."))
        } catch (e: java.net.SocketTimeoutException) {
            emit(Resource.Error("Koneksi timeout. Silakan coba lagi."))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Terjadi kesalahan tidak terduga"))
        }
    }
    
    override fun markAsRead(notificationId: Long): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        
        try {
            val response = notificationApi.markAsRead(notificationId)
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    emit(Resource.Success(Unit))
                } else {
                    emit(Resource.Error(apiResponse?.message ?: "Gagal menandai notifikasi"))
                }
            } else {
                emit(Resource.Error("Gagal menandai notifikasi (${response.code()})"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Terjadi kesalahan"))
        }
    }
    
    override fun markAllAsRead(): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        
        try {
            val response = notificationApi.markAllAsRead()
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    emit(Resource.Success(Unit))
                } else {
                    emit(Resource.Error(apiResponse?.message ?: "Gagal menandai semua notifikasi"))
                }
            } else {
                emit(Resource.Error("Gagal menandai semua notifikasi (${response.code()})"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Terjadi kesalahan"))
        }
    }
}
