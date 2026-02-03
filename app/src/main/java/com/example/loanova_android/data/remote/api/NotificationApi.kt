package com.example.loanova_android.data.remote.api

import com.example.loanova_android.core.base.ApiResponse
import com.example.loanova_android.data.model.dto.NotificationResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Retrofit API Interface untuk Notification endpoints.
 * Base Path: /api/notifications
 */
interface NotificationApi {
    
    /**
     * Get daftar notifikasi user yang sedang login.
     * GET /api/notifications
     */
    @GET("api/notifications")
    suspend fun getMyNotifications(): Response<ApiResponse<List<NotificationResponse>>>
    
    /**
     * Tandai notifikasi spesifik sebagai sudah dibaca.
     * PUT /api/notifications/{id}/read
     */
    @PUT("api/notifications/{id}/read")
    suspend fun markAsRead(@Path("id") notificationId: Long): Response<ApiResponse<Unit>>
    
    /**
     * Tandai semua notifikasi sebagai sudah dibaca.
     * PUT /api/notifications/read-all
     */
    @PUT("api/notifications/read-all")
    suspend fun markAllAsRead(): Response<ApiResponse<Unit>>
}
