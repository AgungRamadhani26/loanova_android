package com.example.loanova_android.ui.features.loan.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.loanova_android.data.model.dto.ApplicationHistoryResponse
import com.example.loanova_android.data.model.dto.LoanApplicationResponse
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// Color Palette
private val HistoryPrimaryColor = Color(0xFF1E3A5F)
private val HistorySecondaryColor = Color(0xFF3B82F6)
private val HistoryBackgroundColor = Color(0xFFF0F9FF)

/**
 * Loan History Screen - Menampilkan riwayat proses pengajuan pinjaman.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanHistoryScreen(
    onNavigateBack: (() -> Unit)? = null,
    showBackButton: Boolean = true,
    viewModel: LoanHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Riwayat Pengajuan",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    if (showBackButton && onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Kembali",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HistoryPrimaryColor
                )
            )
        },
        containerColor = HistoryBackgroundColor
    ) { padding ->
        when {
            uiState.isLoading -> {
                LoadingState(modifier = Modifier.padding(padding))
            }
            uiState.error != null && uiState.latestLoan == null -> {
                ErrorState(
                    error = uiState.error!!,
                    onRetry = { viewModel.retry() },
                    modifier = Modifier.padding(padding)
                )
            }
            uiState.loans.isEmpty() -> {
                EmptyState(modifier = Modifier.padding(padding))
            }
            else -> {
                HistoryContent(
                    uiState = uiState,
                    onSelectLoan = { viewModel.selectLoan(it) },
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = HistorySecondaryColor,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Memuat riwayat...",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun ErrorState(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFEBEE)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.ErrorOutline,
                    contentDescription = null,
                    tint = Color(0xFFE53935),
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                "Gagal Memuat Data",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black.copy(alpha = 0.85f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                error,
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = HistorySecondaryColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Coba Lagi", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(HistorySecondaryColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.History,
                    contentDescription = null,
                    tint = HistorySecondaryColor,
                    modifier = Modifier.size(50.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Belum Ada Riwayat",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = HistoryPrimaryColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Anda belum memiliki pengajuan pinjaman.\nAjukan pinjaman untuk melihat riwayat.",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun HistoryContent(
    uiState: LoanHistoryUiState,
    onSelectLoan: (LoanApplicationResponse) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Loan Selector (if multiple loans)
        if (uiState.loans.size > 1) {
            item {
                LoanSelector(
                    loans = uiState.loans,
                    selectedLoan = uiState.latestLoan,
                    onSelectLoan = onSelectLoan
                )
            }
        }
        
        // Current Loan Info
        uiState.latestLoan?.let { loan ->
            item {
                CurrentLoanCard(loan = loan)
            }
        }
        
        // Progress Tracker
        item {
            ProgressTracker(
                currentStatus = uiState.latestLoan?.status ?: ""
            )
        }
        
        // History Timeline
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Timeline,
                            contentDescription = null,
                            tint = HistorySecondaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Timeline Proses",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = HistoryPrimaryColor
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (uiState.isLoadingHistory) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = HistorySecondaryColor,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    } else if (uiState.history.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Belum ada riwayat perubahan",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        HistoryTimeline(history = uiState.history)
                    }
                }
            }
        }
        
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun LoanSelector(
    loans: List<LoanApplicationResponse>,
    selectedLoan: LoanApplicationResponse?,
    onSelectLoan: (LoanApplicationResponse) -> Unit
) {
    Column {
        Text(
            "Pilih Pengajuan",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = HistoryPrimaryColor
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(loans) { loan ->
                val isSelected = selectedLoan?.id == loan.id
                Surface(
                    modifier = Modifier.clickable { onSelectLoan(loan) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) HistorySecondaryColor else Color.White,
                    shadowElevation = if (isSelected) 4.dp else 1.dp
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            loan.plafondName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else HistoryPrimaryColor
                        )
                        Text(
                            formatCurrency(loan.amount),
                            fontSize = 11.sp,
                            color = if (isSelected) Color.White.copy(alpha = 0.8f) else Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrentLoanCard(loan: LoanApplicationResponse) {
    val statusInfo = getStatusInfo(loan.status)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        loan.plafondName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = HistoryPrimaryColor
                    )
                    Text(
                        formatCurrency(loan.amount),
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                
                // Status Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusInfo.backgroundColor
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            statusInfo.icon,
                            contentDescription = null,
                            tint = statusInfo.color,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            statusInfo.label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusInfo.color
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoItem(label = "Tenor", value = "${loan.tenor} Bulan")
                InfoItem(label = "Tanggal Ajuan", value = formatDate(loan.submittedAt))
            }
        }
    }
}

@Composable
private fun InfoItem(label: String, value: String) {
    Column {
        Text(
            label,
            fontSize = 11.sp,
            color = Color.Gray
        )
        Text(
            value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = HistoryPrimaryColor
        )
    }
}

@Composable
private fun ProgressTracker(currentStatus: String) {
    val steps = listOf(
        ProgressStep("PENDING_REVIEW", "Pengajuan", Icons.Outlined.Description),
        ProgressStep("WAITING_APPROVAL", "Review Marketing", Icons.Outlined.RateReview),
        ProgressStep("WAITING_DISBURSEMENT", "Approval BM", Icons.Outlined.Approval),
        ProgressStep("DISBURSED", "Pencairan", Icons.Outlined.Payments)
    )
    
    val currentStepIndex = steps.indexOfFirst { it.status == currentStatus }
    val isRejected = currentStatus.startsWith("REJECTED")
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.TrendingUp,
                    contentDescription = null,
                    tint = HistorySecondaryColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Progress Pengajuan",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = HistoryPrimaryColor
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            if (isRejected) {
                // Show rejected state
                RejectedProgress(status = currentStatus)
            } else {
                // Show normal progress
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    steps.forEachIndexed { index, step ->
                        val isCompleted = index <= currentStepIndex
                        val isCurrent = index == currentStepIndex
                        
                        ProgressStepItem(
                            step = step,
                            isCompleted = isCompleted,
                            isCurrent = isCurrent,
                            isLast = index == steps.lastIndex,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressStepItem(
    step: ProgressStep,
    isCompleted: Boolean,
    isCurrent: Boolean,
    isLast: Boolean,
    modifier: Modifier = Modifier
) {
    val color = when {
        isCompleted -> Color(0xFF10B981)
        else -> Color.LightGray
    }
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    if (isCompleted) color.copy(alpha = 0.15f)
                    else Color.LightGray.copy(alpha = 0.2f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (isCompleted && !isCurrent) Icons.Default.Check else step.icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            step.label,
            fontSize = 9.sp,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
            color = if (isCompleted) HistoryPrimaryColor else Color.Gray,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun RejectedProgress(status: String) {
    val rejectionInfo = when {
        status.contains("MARKETING") -> "Marketing" to "Pengajuan Anda ditolak oleh Marketing"
        status.contains("BRANCH_MANAGER") -> "Branch Manager" to "Pengajuan Anda ditolak oleh Branch Manager"
        status.contains("BACKOFFICE") -> "Backoffice" to "Pengajuan Anda ditolak oleh Backoffice"
        else -> "Sistem" to "Pengajuan Anda ditolak"
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFEE2E2))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFFEF4444).copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.Cancel,
                contentDescription = null,
                tint = Color(0xFFEF4444),
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                "Ditolak oleh ${rejectionInfo.first}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFEF4444)
            )
            Text(
                rejectionInfo.second,
                fontSize = 12.sp,
                color = Color(0xFFB91C1C)
            )
        }
    }
}

@Composable
private fun HistoryTimeline(history: List<ApplicationHistoryResponse>) {
    Column {
        history.forEachIndexed { index, item ->
            HistoryTimelineItem(
                item = item,
                isFirst = index == 0,
                isLast = index == history.lastIndex
            )
        }
    }
}

@Composable
private fun HistoryTimelineItem(
    item: ApplicationHistoryResponse,
    isFirst: Boolean,
    isLast: Boolean
) {
    val statusInfo = getStatusInfo(item.status)
    
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Timeline indicator
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
            // Top line
            if (!isFirst) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(8.dp)
                        .background(Color.LightGray)
                )
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Dot
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(statusInfo.color)
            )
            
            // Bottom line
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(60.dp)
                        .background(Color.LightGray)
                )
            }
        }
        
        // Content
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp, bottom = if (isLast) 0.dp else 16.dp)
        ) {
            Text(
                statusInfo.label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = statusInfo.color
            )
            Text(
                "oleh ${item.actionByUsername} (${formatRole(item.actionByRole)})",
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                formatDateTime(item.createdAt),
                fontSize = 11.sp,
                color = Color.Gray.copy(alpha = 0.7f)
            )
            if (!item.comment.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = HistoryBackgroundColor
                ) {
                    Text(
                        "\"${item.comment}\"",
                        modifier = Modifier.padding(8.dp),
                        fontSize = 12.sp,
                        color = HistoryPrimaryColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

private data class ProgressStep(
    val status: String,
    val label: String,
    val icon: ImageVector
)

private data class StatusInfo(
    val label: String,
    val color: Color,
    val backgroundColor: Color,
    val icon: ImageVector
)

private fun getStatusInfo(status: String): StatusInfo {
    return when (status.uppercase()) {
        "PENDING_REVIEW" -> StatusInfo(
            label = "Menunggu Review",
            color = Color(0xFFF59E0B),
            backgroundColor = Color(0xFFFEF3C7),
            icon = Icons.Outlined.HourglassTop
        )
        "WAITING_APPROVAL" -> StatusInfo(
            label = "Menunggu Approval",
            color = Color(0xFF3B82F6),
            backgroundColor = Color(0xFFDBEAFE),
            icon = Icons.Outlined.Pending
        )
        "WAITING_DISBURSEMENT" -> StatusInfo(
            label = "Menunggu Pencairan",
            color = Color(0xFF8B5CF6),
            backgroundColor = Color(0xFFEDE9FE),
            icon = Icons.Outlined.AccountBalance
        )
        "DISBURSED" -> StatusInfo(
            label = "Dicairkan",
            color = Color(0xFF10B981),
            backgroundColor = Color(0xFFD1FAE5),
            icon = Icons.Outlined.CheckCircle
        )
        "REJECTED_BY_MARKETING" -> StatusInfo(
            label = "Ditolak Marketing",
            color = Color(0xFFEF4444),
            backgroundColor = Color(0xFFFEE2E2),
            icon = Icons.Outlined.Cancel
        )
        "REJECTED_BY_BRANCH_MANAGER" -> StatusInfo(
            label = "Ditolak BM",
            color = Color(0xFFEF4444),
            backgroundColor = Color(0xFFFEE2E2),
            icon = Icons.Outlined.Cancel
        )
        "REJECTED_BY_BACKOFFICE" -> StatusInfo(
            label = "Ditolak Backoffice",
            color = Color(0xFFEF4444),
            backgroundColor = Color(0xFFFEE2E2),
            icon = Icons.Outlined.Cancel
        )
        else -> StatusInfo(
            label = status.replace("_", " "),
            color = Color.Gray,
            backgroundColor = Color.LightGray.copy(alpha = 0.3f),
            icon = Icons.Outlined.Info
        )
    }
}

private fun formatCurrency(amount: BigDecimal): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    format.maximumFractionDigits = 0
    return format.format(amount).replace("Rp", "Rp ")
}

private fun formatDate(dateTimeString: String): String {
    return try {
        val parser = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        val dateTime = LocalDateTime.parse(dateTimeString, parser)
        dateTime.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("id", "ID")))
    } catch (e: Exception) {
        dateTimeString
    }
}

private fun formatDateTime(dateTimeString: String): String {
    return try {
        val parser = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        val dateTime = LocalDateTime.parse(dateTimeString, parser)
        dateTime.format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale("id", "ID")))
    } catch (e: Exception) {
        dateTimeString
    }
}

private fun formatRole(role: String): String {
    return when (role.uppercase()) {
        "CUSTOMER" -> "Customer"
        "MARKETING" -> "Marketing"
        "BRANCHMANAGER" -> "Branch Manager"
        "BACKOFFICE" -> "Backoffice"
        "SUPERADMIN" -> "Super Admin"
        else -> role
    }
}
