package com.example.loanova_android.ui.features.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loanova_android.core.common.Resource
import com.example.loanova_android.domain.model.Notification
import com.example.loanova_android.domain.repository.INotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State untuk NotificationScreen
 */
data class NotificationUiState(
    val isLoading: Boolean = false,
    val notifications: List<Notification> = emptyList(),
    val error: String? = null,
    val isMarkingRead: Boolean = false,
    val markReadSuccess: Boolean = false,
    val unreadCount: Int = 0,
    val isFromCache: Boolean = false, // Menandakan data berasal dari cache
    val isSyncing: Boolean = false    // Menandakan sedang sync dengan server
)

/**
 * ViewModel untuk NotificationScreen.
 * Menghandle fetching notifikasi dengan offline-first architecture.
 * 
 * Flow:
 * 1. Load data dari cache lokal terlebih dahulu (instant display)
 * 2. Sync dengan server di background
 * 3. Update UI dengan data terbaru
 */
@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: INotificationRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()
    
    init {
        loadNotifications()
    }
    
    /**
     * Load daftar notifikasi (offline-first)
     * 
     * 1. Tampilkan data dari cache dulu (isFromCache = true)
     * 2. Fetch dari server
     * 3. Update dengan data fresh (isFromCache = false)
     */
    fun loadNotifications() {
        viewModelScope.launch {
            notificationRepository.getMyNotifications().collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = _uiState.value.notifications.isEmpty(), // Only show loading if no cached data
                            isSyncing = true,
                            error = null
                        )
                    }
                    is Resource.Success -> {
                        val notifications = result.data ?: emptyList()
                        val unreadCount = notifications.count { !it.isRead }
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isSyncing = false,
                            notifications = notifications,
                            error = null,
                            unreadCount = unreadCount,
                            isFromCache = result.isFromCache
                        )
                    }
                    is Resource.Error -> {
                        // Only show error if no cached data available
                        if (_uiState.value.notifications.isEmpty()) {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isSyncing = false,
                                error = result.message
                            )
                        } else {
                            // We have cached data, just stop syncing indicator
                            _uiState.value = _uiState.value.copy(
                                isSyncing = false
                            )
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Tandai notifikasi sebagai sudah dibaca (optimistic update)
     * 
     * 1. Update local database immediately
     * 2. Sync dengan server di background
     */
    fun markAsRead(notificationId: Long) {
        // Optimistic update - update UI immediately
        val updatedNotifications = _uiState.value.notifications.map { notif ->
            if (notif.id == notificationId) notif.copy(isRead = true) else notif
        }
        val unreadCount = updatedNotifications.count { !it.isRead }
        _uiState.value = _uiState.value.copy(
            notifications = updatedNotifications,
            unreadCount = unreadCount
        )
        
        // Sync with server
        viewModelScope.launch {
            notificationRepository.markAsRead(notificationId).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.value = _uiState.value.copy(isMarkingRead = true)
                    }
                    is Resource.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isMarkingRead = false,
                            markReadSuccess = true
                        )
                    }
                    is Resource.Error -> {
                        // Even if server fails, we keep the optimistic update
                        // Will sync on next fetch
                        _uiState.value = _uiState.value.copy(
                            isMarkingRead = false
                        )
                    }
                }
            }
        }
    }
    
    /**
     * Tandai semua notifikasi sebagai sudah dibaca (optimistic update)
     */
    fun markAllAsRead() {
        // Optimistic update - update UI immediately
        val updatedNotifications = _uiState.value.notifications.map { notif ->
            notif.copy(isRead = true)
        }
        _uiState.value = _uiState.value.copy(
            notifications = updatedNotifications,
            unreadCount = 0
        )
        
        // Sync with server
        viewModelScope.launch {
            notificationRepository.markAllAsRead().collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.value = _uiState.value.copy(isMarkingRead = true)
                    }
                    is Resource.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isMarkingRead = false,
                            markReadSuccess = true
                        )
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isMarkingRead = false
                        )
                    }
                }
            }
        }
    }
    
    /**
     * Clear mark read success flag
     */
    fun clearMarkReadSuccess() {
        _uiState.value = _uiState.value.copy(markReadSuccess = false)
    }
    
    /**
     * Clear error
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
