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
    val unreadCount: Int = 0
)

/**
 * ViewModel untuk NotificationScreen.
 * Menghandle fetching notifikasi dan marking as read.
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
     * Load daftar notifikasi dari server
     */
    fun loadNotifications() {
        viewModelScope.launch {
            notificationRepository.getMyNotifications().collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = true,
                            error = null
                        )
                    }
                    is Resource.Success -> {
                        val notifications = result.data ?: emptyList()
                        val unreadCount = notifications.count { !it.isRead }
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            notifications = notifications,
                            error = null,
                            unreadCount = unreadCount
                        )
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }
    
    /**
     * Tandai notifikasi sebagai sudah dibaca
     */
    fun markAsRead(notificationId: Long) {
        viewModelScope.launch {
            notificationRepository.markAsRead(notificationId).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.value = _uiState.value.copy(isMarkingRead = true)
                    }
                    is Resource.Success -> {
                        // Update local state
                        val updatedNotifications = _uiState.value.notifications.map { notif ->
                            if (notif.id == notificationId) notif.copy(isRead = true) else notif
                        }
                        val unreadCount = updatedNotifications.count { !it.isRead }
                        _uiState.value = _uiState.value.copy(
                            isMarkingRead = false,
                            notifications = updatedNotifications,
                            unreadCount = unreadCount,
                            markReadSuccess = true
                        )
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isMarkingRead = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }
    
    /**
     * Tandai semua notifikasi sebagai sudah dibaca
     */
    fun markAllAsRead() {
        viewModelScope.launch {
            notificationRepository.markAllAsRead().collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.value = _uiState.value.copy(isMarkingRead = true)
                    }
                    is Resource.Success -> {
                        // Update local state - mark all as read
                        val updatedNotifications = _uiState.value.notifications.map { notif ->
                            notif.copy(isRead = true)
                        }
                        _uiState.value = _uiState.value.copy(
                            isMarkingRead = false,
                            notifications = updatedNotifications,
                            unreadCount = 0,
                            markReadSuccess = true
                        )
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isMarkingRead = false,
                            error = result.message
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
