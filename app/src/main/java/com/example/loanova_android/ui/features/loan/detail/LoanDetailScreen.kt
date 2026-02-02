package com.example.loanova_android.ui.features.loan.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.loanova_android.BuildConfig
import com.example.loanova_android.data.model.dto.LoanApplicationResponse
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// Color Palette
private val DetailPrimaryColor = Color(0xFF1E3A5F)
private val DetailSecondaryColor = Color(0xFF3B82F6)
private val DetailBackgroundColor = Color(0xFFF0F9FF)

/**
 * Helper function to build image URL from path
 */
private fun getImageUrl(path: String?): String? {
    if (path.isNullOrEmpty()) return null
    val cleanPath = if (path.startsWith("/")) path.substring(1) else path
    return "${BuildConfig.BASE_URL}uploads/$cleanPath"
}

/**
 * Loan Detail Screen - Menampilkan detail lengkap pengajuan pinjaman.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanDetailScreen(
    loanId: Long,
    onNavigateBack: () -> Unit,
    viewModel: LoanDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    
    // State for image preview dialog
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }
    var selectedImageLabel by remember { mutableStateOf("") }
    
    // Load data if not loaded yet
    LaunchedEffect(loanId) {
        if (uiState.loan == null && !uiState.isLoading) {
            viewModel.loadLoanDetail(loanId)
        }
    }
    
    // Image Preview Dialog
    if (selectedImageUrl != null) {
        ImagePreviewDialog(
            imageUrl = selectedImageUrl!!,
            label = selectedImageLabel,
            onDismiss = { selectedImageUrl = null }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Detail Pengajuan",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DetailPrimaryColor
                )
            )
        },
        containerColor = DetailBackgroundColor
    ) { padding ->
        when {
            uiState.isLoading -> {
                LoadingState(modifier = Modifier.padding(padding))
            }
            uiState.error != null -> {
                ErrorState(
                    error = uiState.error!!,
                    onRetry = { viewModel.retry() },
                    modifier = Modifier.padding(padding)
                )
            }
            uiState.loan != null -> {
                LoanDetailContent(
                    loan = uiState.loan!!,
                    modifier = Modifier.padding(padding),
                    scrollState = scrollState,
                    onImageClick = { url, label ->
                        selectedImageUrl = url
                        selectedImageLabel = label
                    }
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
                color = DetailSecondaryColor,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Memuat detail pengajuan...",
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
                colors = ButtonDefaults.buttonColors(containerColor = DetailSecondaryColor),
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
private fun LoanDetailContent(
    loan: LoanApplicationResponse,
    modifier: Modifier = Modifier,
    scrollState: androidx.compose.foundation.ScrollState,
    onImageClick: (String, String) -> Unit
) {
    val interestRate = loan.interestRateSnapshot ?: BigDecimal("1.0")
    val totalInterest = loan.amount
        .multiply(interestRate)
        .divide(BigDecimal("100"), 2, RoundingMode.HALF_UP)
        .multiply(BigDecimal(loan.tenor))
    val totalRepayment = loan.amount.add(totalInterest)
    val monthlyInstallment = totalRepayment.divide(BigDecimal(loan.tenor), 0, RoundingMode.CEILING)
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Loan Details Section
        SectionCard(
            title = "Detail Pinjaman",
            icon = Icons.Outlined.CreditCard
        ) {
            DetailRow(label = "ID Pengajuan", value = "#${loan.id}", valueColor = DetailSecondaryColor)
            DetailRow(label = "Jenis Plafond", value = loan.plafondName)
            DetailRow(label = "Jumlah Pinjaman", value = formatCurrency(loan.amount))
            DetailRow(label = "Tenor", value = "${loan.tenor} Bulan")
            DetailRow(label = "Bunga", value = "${interestRate}% per bulan")
            DetailRow(label = "Total Bunga", value = formatCurrency(totalInterest))
            DetailRow(label = "Total Pelunasan", value = formatCurrency(totalRepayment), valueColor = Color(0xFFEF4444))
            DetailRow(label = "Cicilan/Bulan", value = formatCurrency(monthlyInstallment), valueColor = DetailSecondaryColor)
            DetailRow(label = "Tanggal Pengajuan", value = formatDateTime(loan.submittedAt))
        }
        
        // Personal Data Section
        SectionCard(
            title = "Data Pribadi",
            icon = Icons.Outlined.Person
        ) {
            DetailRow(label = "Nama Lengkap", value = loan.fullNameSnapshot)
            DetailRow(label = "NIK", value = loan.nikSnapshot)
            DetailRow(label = "Tanggal Lahir", value = formatDate(loan.birthDateSnapshot))
            DetailRow(label = "No. Telepon", value = loan.phoneNumberSnapshot)
            DetailRow(label = "Alamat", value = loan.userAddressSnapshot)
            loan.npwpNumberSnapshot?.let {
                DetailRow(label = "NPWP", value = it)
            }
        }
        
        // Employment Data Section
        SectionCard(
            title = "Data Pekerjaan",
            icon = Icons.Outlined.Work
        ) {
            DetailRow(label = "Pekerjaan", value = loan.occupation)
            loan.companyName?.let {
                DetailRow(label = "Nama Perusahaan", value = it)
            }
        }
        
        // Financial Data Section
        SectionCard(
            title = "Data Keuangan",
            icon = Icons.Outlined.AccountBalance
        ) {
            DetailRow(label = "No. Rekening", value = loan.rekeningNumber)
        }
        
        // Documents Section
        SectionCard(
            title = "Dokumen Pendukung",
            icon = Icons.Outlined.Description
        ) {
            DocumentsList(loan = loan, onImageClick = onImageClick)
        }
        
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Section Header
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = DetailSecondaryColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = DetailPrimaryColor
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))
            
            content()
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: Color = Color.Black.copy(alpha = 0.85f)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            label,
            fontSize = 13.sp,
            color = Color.Gray,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = valueColor,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.6f)
        )
    }
}

@Composable
private fun DocumentsList(
    loan: LoanApplicationResponse,
    onImageClick: (String, String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DocumentListItem(
            label = "KTP",
            imagePath = loan.ktpPhotoSnapshot,
            onImageClick = onImageClick
        )
        DocumentListItem(
            label = "NPWP",
            imagePath = loan.npwpPhotoSnapshot,
            onImageClick = onImageClick
        )
        DocumentListItem(
            label = "Buku Tabungan",
            imagePath = loan.savingBookCover,
            onImageClick = onImageClick
        )
        DocumentListItem(
            label = "Slip Gaji",
            imagePath = loan.payslipPhoto,
            onImageClick = onImageClick
        )
    }
}

@Composable
private fun DocumentListItem(
    label: String,
    imagePath: String?,
    onImageClick: (String, String) -> Unit
) {
    val context = LocalContext.current
    val imageUrl = getImageUrl(imagePath)
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = imageUrl != null) {
                if (imageUrl != null) {
                    onImageClick(imageUrl, label)
                }
            },
        shape = RoundedCornerShape(8.dp),
        color = DetailBackgroundColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            if (imageUrl != null) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageUrl)
                        .crossfade(true)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .networkCachePolicy(CachePolicy.ENABLED)
                        .build(),
                    contentDescription = label,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color.LightGray.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = DetailSecondaryColor
                            )
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color.LightGray.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.BrokenImage,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Image,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Label
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = DetailPrimaryColor
                )
                Text(
                    if (imageUrl != null) "Tap untuk melihat" else "Tidak tersedia",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
            
            // Arrow
            if (imageUrl != null) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ImagePreviewDialog(
    imageUrl: String,
    label: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f))
                .clickable(onClick = onDismiss)
        ) {
            // Close button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Tutup",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            // Label
            Text(
                label,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 20.dp),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            // Image
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .crossfade(true)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .networkCachePolicy(CachePolicy.ENABLED)
                    .build(),
                contentDescription = label,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .align(Alignment.Center),
                contentScale = ContentScale.Fit,
                loading = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                },
                error = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Outlined.BrokenImage,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Gagal memuat gambar",
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            )
        }
    }
}

private fun formatCurrency(amount: BigDecimal): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    format.maximumFractionDigits = 0
    return format.format(amount).replace("Rp", "Rp ")
}

private fun formatDateTime(dateTimeString: String): String {
    return try {
        val parser = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        val dateTime = LocalDateTime.parse(dateTimeString, parser)
        dateTime.format(DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm", Locale("id", "ID")))
    } catch (e: Exception) {
        dateTimeString
    }
}

private fun formatDate(dateString: String?): String {
    if (dateString == null) return "-"
    return try {
        val date = LocalDate.parse(dateString)
        date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("id", "ID")))
    } catch (e: Exception) {
        dateString
    }
}
