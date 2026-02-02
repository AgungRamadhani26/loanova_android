package com.example.loanova_android.ui.features.loan.myloans

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.loanova_android.data.model.dto.LoanApplicationResponse
import com.example.loanova_android.ui.features.loan.detail.LoanDetailScreen
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// Modern Blue Color Palette (consistent with LoanApplicationScreen)
private val MyLoansPrimaryColor = Color(0xFF1E3A5F)      // Deep Navy Blue
private val MyLoansSecondaryColor = Color(0xFF3B82F6)    // Vibrant Blue
private val MyLoansAccentColor = Color(0xFF60A5FA)       // Light Blue Accent
private val MyLoansGradientStart = Color(0xFF0F172A)     // Dark Navy
private val MyLoansGradientMid = Color(0xFF1E3A5F)       // Medium Navy
private val MyLoansGradientEnd = Color(0xFF1E40AF)       // Blue End
private val MyLoansBackgroundColor = Color(0xFFF0F9FF)   // Light Blue Background

/**
 * My Loans Screen - Menampilkan daftar pengajuan pinjaman milik Customer.
 * 
 * Fitur:
 * - List semua loan applications
 * - Informasi: Amount, Tenor, Interest Rate, Plafond, Total Repayment, Status, Submitted Date
 * - Pull to refresh
 * - Empty state & Loading state
 * - Navigate to detail on click
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyLoansScreen(
    onNavigateBack: (() -> Unit)? = null,
    onNavigateToDetail: ((Long) -> Unit)? = null,
    showBackButton: Boolean = false,
    viewModel: MyLoansViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val pullRefreshState = rememberPullToRefreshState()
    
    // State for showing detail screen internally
    var selectedLoanId by remember { mutableStateOf<Long?>(null) }
    
    // If detail screen should be shown
    if (selectedLoanId != null) {
        LoanDetailScreen(
            loanId = selectedLoanId!!,
            onNavigateBack = { 
                selectedLoanId = null 
                // Refresh list when returning from detail
                viewModel.refresh()
            }
        )
        return
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Pinjaman Saya",
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
                    containerColor = MyLoansPrimaryColor
                ),
                actions = {
                    // Refresh button
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        containerColor = MyLoansBackgroundColor
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.refresh() },
            state = pullRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading && uiState.loans.isEmpty() -> {
                    LoadingState()
                }
                uiState.error != null && uiState.loans.isEmpty() -> {
                    ErrorState(
                        error = uiState.error!!,
                        onRetry = { viewModel.loadMyLoans() }
                    )
                }
                uiState.loans.isEmpty() -> {
                    EmptyState()
                }
                else -> {
                    LoansList(
                        loans = uiState.loans,
                        onLoanClick = { loanId ->
                            // Use internal navigation or external callback
                            if (onNavigateToDetail != null) {
                                onNavigateToDetail(loanId)
                            } else {
                                selectedLoanId = loanId
                            }
                        }
                    )
                }
            }
        }
    }
    
    // Error snackbar for refresh errors
    uiState.error?.let { error ->
        if (uiState.loans.isNotEmpty()) {
            LaunchedEffect(error) {
                // Auto clear error after showing
                kotlinx.coroutines.delay(3000)
                viewModel.clearError()
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = MyLoansSecondaryColor,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Memuat data pinjaman...",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun ErrorState(
    error: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
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
                colors = ButtonDefaults.buttonColors(containerColor = MyLoansSecondaryColor),
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
private fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated illustration placeholder
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MyLoansSecondaryColor.copy(alpha = 0.1f),
                                MyLoansAccentColor.copy(alpha = 0.1f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.CreditCard,
                    contentDescription = null,
                    tint = MyLoansSecondaryColor,
                    modifier = Modifier.size(56.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                "Belum Ada Pengajuan",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MyLoansPrimaryColor
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                "Anda belum memiliki pengajuan pinjaman.\nAjukan pinjaman pertama Anda sekarang!",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Decorative tip card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFF3E0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("💡", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Tips:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF9800)
                        )
                        Text(
                            "Pastikan profil Anda sudah lengkap sebelum mengajukan pinjaman.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoansList(
    loans: List<LoanApplicationResponse>,
    onLoanClick: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Summary Header
        item {
            LoansSummaryCard(loans = loans)
        }
        
        // Loans list
        itemsIndexed(loans) { index, loan ->
            LoanApplicationCard(
                loan = loan,
                index = index,
                onClick = { onLoanClick(loan.id) }
            )
        }
        
        // Footer spacer
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun LoansSummaryCard(loans: List<LoanApplicationResponse>) {
    val totalAmount = loans.fold(BigDecimal.ZERO) { acc, loan -> acc.add(loan.amount) }
    val pendingCount = loans.count { it.status in listOf("PENDING_REVIEW", "WAITING_APPROVAL", "WAITING_DISBURSEMENT") }
    val approvedCount = loans.count { it.status == "DISBURSED" }
    val rejectedCount = loans.count { it.status.contains("REJECTED", ignoreCase = true) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(MyLoansGradientStart, MyLoansGradientMid, MyLoansGradientEnd)
                    )
                )
        ) {
            // Decorative circles
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.08f),
                    center = Offset(size.width * 0.9f, size.height * 0.2f),
                    radius = size.width * 0.3f
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.05f),
                    center = Offset(size.width * 0.1f, size.height * 0.8f),
                    radius = size.width * 0.2f
                )
            }
            
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Assessment,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Ringkasan Pinjaman",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SummaryStatItem(
                        label = "Total Pengajuan",
                        value = "${loans.size}",
                        icon = Icons.Outlined.Description
                    )
                    SummaryStatItem(
                        label = "Diproses",
                        value = "$pendingCount",
                        icon = Icons.Outlined.HourglassTop
                    )
                    SummaryStatItem(
                        label = "Dicairkan",
                        value = "$approvedCount",
                        icon = Icons.Outlined.CheckCircle
                    )
                    SummaryStatItem(
                        label = "Ditolak",
                        value = "$rejectedCount",
                        icon = Icons.Outlined.Cancel
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryStatItem(
    label: String,
    value: String,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            label,
            fontSize = 9.sp,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun LoanApplicationCard(
    loan: LoanApplicationResponse,
    index: Int,
    onClick: () -> Unit
) {
    // Calculate total repayment
    val interestRate = loan.interestRateSnapshot ?: BigDecimal("1.0") // Default 1% if null
    val totalInterest = loan.amount
        .multiply(interestRate)
        .divide(BigDecimal("100"), 2, RoundingMode.HALF_UP)
        .multiply(BigDecimal(loan.tenor))
    val totalRepayment = loan.amount.add(totalInterest)
    
    // Parse and format date
    val formattedDate = try {
        val parser = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        val dateTime = LocalDateTime.parse(loan.submittedAt, parser)
        dateTime.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("id", "ID")))
    } catch (e: Exception) {
        loan.submittedAt
    }
    
    // Get status info
    val statusInfo = getStatusInfo(loan.status)
    
    // Get plafond color
    val plafondColor = getPlafondThemeColor(loan.plafondName)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Plafond Badge
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(plafondColor, plafondColor.copy(alpha = 0.7f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    loan.plafondName.take(1),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Middle: Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Plafond name & Status row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        loan.plafondName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MyLoansPrimaryColor
                    )
                    
                    // Status Badge (compact)
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = statusInfo.backgroundColor
                    ) {
                        Text(
                            statusInfo.label,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusInfo.color
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Amount
                Text(
                    formatCurrency(loan.amount),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black.copy(alpha = 0.85f)
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                // Details row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tenor
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.DateRange,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            "${loan.tenor} bln",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                    
                    // Interest
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Percent,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            "${interestRate}%",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                    
                    // Date
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            formattedDate,
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                // Total Repayment
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Total bayar: ",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Text(
                        formatCurrency(totalRepayment),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFEF4444)
                    )
                }
            }
            
            // Right: Arrow indicator
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// Status information data class
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

private fun getPlafondThemeColor(plafondName: String): Color {
    return when {
        plafondName.contains("Gold", ignoreCase = true) -> Color(0xFFFFC107)
        plafondName.contains("Silver", ignoreCase = true) -> Color(0xFF9E9E9E)
        plafondName.contains("Bronze", ignoreCase = true) -> Color(0xFFD84315)
        plafondName.contains("Platinum", ignoreCase = true) -> Color(0xFF00BCD4)
        else -> MyLoansSecondaryColor
    }
}

private fun formatCurrency(amount: BigDecimal): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    format.maximumFractionDigits = 0
    return format.format(amount).replace("Rp", "Rp ")
}
