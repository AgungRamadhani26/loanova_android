package com.example.loanova_android.ui.features.notification

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.loanova_android.domain.model.Notification
import com.example.loanova_android.ui.theme.LoanovaBlue

// Color Palette for Notification Screen
private val NotifPrimaryColor = Color(0xFF1E3A5F)      // Deep Navy Blue
private val NotifSecondaryColor = Color(0xFF3B82F6)    // Vibrant Blue
private val NotifAccentColor = Color(0xFF60A5FA)       // Light Blue Accent
private val NotifSuccessColor = Color(0xFF10B981)      // Green for success
private val NotifWarningColor = Color(0xFFF59E0B)      // Amber for warning/pending
private val NotifErrorColor = Color(0xFFEF4444)        // Red for rejected
private val NotifBackgroundColor = Color(0xFFF0F9FF)   // Light Blue Background

/**
 * Notification Screen - Menampilkan daftar notifikasi perkembangan pinjaman.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    showBackButton: Boolean = false,
    onNavigateBack: (() -> Unit)? = null,
    onNavigateToLoanHistory: ((loanApplicationId: Long?) -> Unit)? = null,
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }
    
    // Handle refresh
    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) {
            isRefreshing = false
        }
    }
    
    Scaffold(
        topBar = {
            NotificationTopBar(
                unreadCount = uiState.unreadCount,
                showBackButton = showBackButton,
                onNavigateBack = onNavigateBack,
                onMarkAllRead = { viewModel.markAllAsRead() },
                onRefresh = { viewModel.loadNotifications() },
                isMarkingRead = uiState.isMarkingRead
            )
        },
        containerColor = NotifBackgroundColor
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                viewModel.loadNotifications()
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading && uiState.notifications.isEmpty() -> {
                    // Initial loading state
                    LoadingContent()
                }
                uiState.error != null && uiState.notifications.isEmpty() -> {
                    // Error state
                    ErrorContent(
                        error = uiState.error!!,
                        onRetry = { viewModel.loadNotifications() }
                    )
                }
                uiState.notifications.isEmpty() -> {
                    // Empty state
                    EmptyNotificationContent()
                }
                else -> {
                    // Notification list
                    NotificationList(
                        notifications = uiState.notifications,
                        onNotificationClick = { notification ->
                            // Mark as read if unread
                            if (!notification.isRead) {
                                viewModel.markAsRead(notification.id)
                            }
                            // Navigate to loan history with the specific loan application ID
                            onNavigateToLoanHistory?.invoke(notification.loanApplicationId)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationTopBar(
    unreadCount: Int,
    showBackButton: Boolean,
    onNavigateBack: (() -> Unit)?,
    onMarkAllRead: () -> Unit,
    onRefresh: () -> Unit,
    isMarkingRead: Boolean
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Notifikasi",
                    fontWeight = FontWeight.Bold
                )
                if (unreadCount > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Badge(
                        containerColor = NotifErrorColor,
                        contentColor = Color.White
                    ) {
                        Text(
                            text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        },
        navigationIcon = {
            if (showBackButton && onNavigateBack != null) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Kembali"
                    )
                }
            }
        },
        actions = {
            // Refresh button
            IconButton(onClick = onRefresh) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = Color.White
                )
            }
            
            // Mark all as read button
            if (unreadCount > 0) {
                IconButton(
                    onClick = onMarkAllRead,
                    enabled = !isMarkingRead
                ) {
                    if (isMarkingRead) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Default.DoneAll,
                            contentDescription = "Tandai Semua Dibaca",
                            tint = Color.White
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = NotifPrimaryColor,
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White
        )
    )
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                color = NotifSecondaryColor,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Memuat notifikasi...",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(NotifErrorColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = NotifErrorColor,
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Gagal Memuat",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = NotifPrimaryColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                color = Color.Gray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = NotifSecondaryColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Coba Lagi")
            }
        }
    }
}

@Composable
private fun EmptyNotificationContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            // Animated bell icon
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                NotifSecondaryColor.copy(alpha = 0.1f),
                                NotifAccentColor.copy(alpha = 0.1f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.NotificationsNone,
                    contentDescription = null,
                    tint = NotifSecondaryColor,
                    modifier = Modifier.size(50.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Belum Ada Notifikasi",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = NotifPrimaryColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Notifikasi perkembangan pengajuan\npinjaman Anda akan muncul di sini",
                color = Color.Gray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun NotificationList(
    notifications: List<Notification>,
    onNotificationClick: (Notification) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Today's notifications
        val today = notifications.filter { 
            it.createdAt.toLocalDate() == java.time.LocalDate.now() 
        }
        if (today.isNotEmpty()) {
            item {
                SectionHeader(title = "Hari Ini", count = today.size)
            }
            items(today, key = { it.id }) { notification ->
                NotificationCard(
                    notification = notification,
                    onClick = { onNotificationClick(notification) }
                )
            }
        }
        
        // Earlier notifications
        val earlier = notifications.filter { 
            it.createdAt.toLocalDate() != java.time.LocalDate.now() 
        }
        if (earlier.isNotEmpty()) {
            item {
                if (today.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
                SectionHeader(title = "Sebelumnya", count = earlier.size)
            }
            items(earlier, key = { it.id }) { notification ->
                NotificationCard(
                    notification = notification,
                    onClick = { onNotificationClick(notification) }
                )
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = Color.Gray
        )
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = NotifSecondaryColor.copy(alpha = 0.1f)
        ) {
            Text(
                text = "$count notifikasi",
                fontSize = 11.sp,
                color = NotifSecondaryColor,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun NotificationCard(
    notification: Notification,
    onClick: () -> Unit
) {
    val (icon, iconColor, bgColor) = getNotificationStyle(notification.title)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) Color.White else Color(0xFFF0F9FF)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (notification.isRead) 1.dp else 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icon indicator
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Content
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        fontWeight = if (notification.isRead) FontWeight.Medium else FontWeight.Bold,
                        fontSize = 14.sp,
                        color = NotifPrimaryColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Unread indicator
                    if (!notification.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(NotifSecondaryColor)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = notification.message,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    lineHeight = 18.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Timestamp
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = Color.LightGray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = notification.getRelativeTime(),
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )
                }
            }
        }
    }
}

/**
 * Get notification style based on title/type
 */
private fun getNotificationStyle(title: String): Triple<ImageVector, Color, Color> {
    return when {
        title.contains("Cair", ignoreCase = true) || title.contains("Disburs", ignoreCase = true) -> {
            Triple(Icons.Default.CheckCircle, NotifSuccessColor, NotifSuccessColor.copy(alpha = 0.1f))
        }
        title.contains("Disetujui", ignoreCase = true) || title.contains("Approve", ignoreCase = true) -> {
            Triple(Icons.Default.ThumbUp, NotifSuccessColor, NotifSuccessColor.copy(alpha = 0.1f))
        }
        title.contains("Diproses", ignoreCase = true) || title.contains("Menunggu", ignoreCase = true) -> {
            Triple(Icons.Default.HourglassTop, NotifWarningColor, NotifWarningColor.copy(alpha = 0.1f))
        }
        title.contains("Ditolak", ignoreCase = true) || title.contains("Reject", ignoreCase = true) -> {
            Triple(Icons.Default.Cancel, NotifErrorColor, NotifErrorColor.copy(alpha = 0.1f))
        }
        title.contains("Plafond", ignoreCase = true) -> {
            Triple(Icons.Default.AccountBalanceWallet, NotifSecondaryColor, NotifSecondaryColor.copy(alpha = 0.1f))
        }
        else -> {
            Triple(Icons.Default.Notifications, NotifSecondaryColor, NotifSecondaryColor.copy(alpha = 0.1f))
        }
    }
}
