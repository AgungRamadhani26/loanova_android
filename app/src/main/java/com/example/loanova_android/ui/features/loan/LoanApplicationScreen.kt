package com.example.loanova_android.ui.features.loan

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.loanova_android.core.common.ImageUtils
import com.example.loanova_android.ui.theme.*
import com.example.loanova_android.data.model.dto.UserPlafondResponse
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Objects
import java.text.NumberFormat
import java.util.Locale

// Modern Blue Color Palette for Loan Application
private val LoanPrimaryColor = Color(0xFF1E3A5F)      // Deep Navy Blue
private val LoanSecondaryColor = Color(0xFF3B82F6)    // Vibrant Blue
private val LoanAccentColor = Color(0xFF60A5FA)       // Light Blue Accent
private val LoanGradientStart = Color(0xFF0F172A)     // Dark Navy
private val LoanGradientMid = Color(0xFF1E3A5F)       // Medium Navy
private val LoanGradientEnd = Color(0xFF1E40AF)       // Blue End
private val LoanBackgroundColor = Color(0xFFF0F9FF)   // Light Blue Background

/**
 * Loan Application Screen - Form untuk mengajukan pinjaman.
 * 
 * Fitur:
 * - Dropdown untuk memilih Branch dan Plafond
 * - Slider + Input Manual untuk Amount dan Tenor
 * - File upload untuk dokumen
 * - Auto capture GPS location
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanApplicationScreen(
    onNavigateBack: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: LoanApplicationViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    
    // Location
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    
    // Permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        
        if (fineGranted || coarseGranted) {
            getLocation(
                context, 
                fusedLocationClient,
                onLocationReceived = { lat, lng ->
                    viewModel.updateLocation(lat, lng)
                },
                onFailure = { error ->
                    viewModel.setLocationError(error)
                }
            )
        } else {
            viewModel.setLocationError("Izin lokasi diperlukan untuk mengajukan pinjaman")
        }
    }
    
    // File picker for saving book cover
    var savingBookCoverFile by remember { mutableStateOf<File?>(null) }
    var payslipPhotoFile by remember { mutableStateOf<File?>(null) }
    
    // --- CAMERA & GALLERY LOGIC ---
    // Dialog state: Apakah popup "Pilih Kamera/Galeri" sedang muncul?
    var showImageSourceDialog by remember { mutableStateOf(false) }
    
    // Target state: Foto mana yang sedang diedit? ("savingBook" atau "payslip")
    var currentImageTarget by remember { mutableStateOf<String?>(null) }
    
    // Temp URI: Menyimpan lokasi sementara foto hasil jepretan kamera sebelum diproses
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var tempCameraPath by remember { mutableStateOf<String?>(null) }
    
    /**
     * Membuat URI sementara untuk menyimpan hasil foto kamera.
     * Menggunakan FileProvider agar aplikasi Kamera eksternal (bawaan HP)
     * bisa mengakses file di folder cache aplikasi kita secara aman.
     */
    fun createTempPictureUri(): Uri {
        val tempFile = File.createTempFile("camera_loan_", ".jpg", context.cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }
        tempCameraPath = tempFile.absolutePath
        return FileProvider.getUriForFile(
            Objects.requireNonNull(context),
            "com.example.loanova_android.provider",
            tempFile
        )
    }
    
    // Gallery Launcher (dipindahkan ke sini untuk mendukung keduanya)
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            when(currentImageTarget) {
                "savingBook" -> {
                    viewModel.updateSavingBookCover(it)
                    savingBookCoverFile = uriToFile(context, it)
                }
                "payslip" -> {
                    viewModel.updatePayslipPhoto(it)
                    payslipPhotoFile = uriToFile(context, it)
                }
            }
        }
    }
    
    // Camera Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraPath != null) {
            val rawFile = File(tempCameraPath!!)
            
            // Process (Rotate & Compress) Immediately using ImageUtils
            val processedFile = ImageUtils.processFile(rawFile)
            
            if (processedFile != null) {
                val newUri = Uri.fromFile(processedFile)
                when(currentImageTarget) {
                    "savingBook" -> {
                        viewModel.updateSavingBookCover(newUri)
                        savingBookCoverFile = processedFile
                    }
                    "payslip" -> {
                        viewModel.updatePayslipPhoto(newUri)
                        payslipPhotoFile = processedFile
                    }
                }
            } else {
                Toast.makeText(context, "Gagal memproses gambar", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    // Camera Permission Launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                val uri = createTempPictureUri()
                tempCameraUri = uri
                cameraLauncher.launch(uri)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Gagal membuka kamera: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(context, "Izin kamera diperlukan untuk mengambil foto", Toast.LENGTH_SHORT).show()
        }
    }
    
    // Function to open image source dialog
    fun openImageSelection(target: String) {
        currentImageTarget = target
        showImageSourceDialog = true
    }
    
    // Request location on first load
    LaunchedEffect(Unit) {
        val fineLocation = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        )
        val coarseLocation = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        )
        
        if (fineLocation == PackageManager.PERMISSION_GRANTED || 
            coarseLocation == PackageManager.PERMISSION_GRANTED) {
            getLocation(
                context, 
                fusedLocationClient,
                onLocationReceived = { lat, lng ->
                    viewModel.updateLocation(lat, lng)
                },
                onFailure = { error ->
                    viewModel.setLocationError(error)
                }
            )
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
    
    // Handle success
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            Toast.makeText(context, uiState.successMessage, Toast.LENGTH_LONG).show()
            onSuccess()
        }
    }
    
    // Handle error
    // Success message logic remains as Toast
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
    }
    
    // Image Source Dialog (Camera/Gallery Picker)
    if (showImageSourceDialog) {
        ImageSourceOptionDialog(
            onDismiss = { showImageSourceDialog = false },
            onCameraClick = {
                showImageSourceDialog = false
                val permissionToCheck = Manifest.permission.CAMERA
                val isGranted = ContextCompat.checkSelfPermission(
                    context,
                    permissionToCheck
                ) == PackageManager.PERMISSION_GRANTED
                
                if (isGranted) {
                    try {
                        val uri = createTempPictureUri()
                        tempCameraUri = uri
                        cameraLauncher.launch(uri)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(context, "Gagal membuka kamera: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                } else {
                    cameraPermissionLauncher.launch(permissionToCheck)
                }
            },
            onGalleryClick = {
                showImageSourceDialog = false
                galleryLauncher.launch("image/*")
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajukan Pinjaman", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LoanPrimaryColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(LoanBackgroundColor)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Offline Queued Banner
                AnimatedVisibility(
                    visible = uiState.isOfflineQueued,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    OfflineQueuedBanner()
                }
                
                // Error Banner
                AnimatedVisibility(
                    visible = uiState.errorMessage != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Surface(
                        color = Color(0xFFFDE8E8), // Pink background
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.2f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = uiState.errorMessage ?: "",
                                color = Color(0xFFEF4444),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // CARD 1: INFORMASI & LOKASI
                FormCard(title = "Informasi & Lokasi") {
                    LocationStatusCard(
                        latitude = uiState.latitude,
                        longitude = uiState.longitude,
                        error = uiState.fieldErrors?.get("latitude") ?: uiState.fieldErrors?.get("longitude") ?: uiState.locationError,
                        onRefresh = {
                            getLocation(
                                context, 
                                fusedLocationClient,
                                onLocationReceived = { lat, lng ->
                                    viewModel.updateLocation(lat, lng)
                                },
                                onFailure = { error ->
                                    viewModel.setLocationError(error)
                                }
                            )
                        }
                    )
                    
                    if (uiState.isLoadingPlafonds) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 12.dp))
                    } else if (uiState.activePlafond != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        ActivePlafondCard(
                            plafondName = uiState.activePlafond!!.plafondName,
                            remainingAmount = uiState.activePlafond!!.remainingAmount
                        )
                    }

                    if (uiState.isLoadingBranches) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 12.dp))
                    } else {
                        Spacer(modifier = Modifier.height(16.dp))
                        DropdownCard(
                            label = "Cabang Terdekat",
                            options = uiState.branches.map { it.id to "${it.branchCode} - ${it.branchName}" },
                            selectedId = uiState.selectedBranchId,
                            onSelect = { viewModel.selectBranch(it) },
                            error = uiState.fieldErrors?.get("branchId")
                        )
                    }
                }
                
                // CARD 2: PENGATURAN PINJAMAN
                if (uiState.selectedPlafondId != null) {
                    FormCard(title = "Rincian Pinjaman") {
                        AmountSliderCard(
                            amount = uiState.amount,
                            minAmount = uiState.minAmount,
                            maxAmount = uiState.maxAmount,
                            onAmountChange = { viewModel.updateAmount(it) },
                            onSliderChange = { viewModel.updateAmountFromSlider(it) },
                            error = uiState.fieldErrors?.get("amount")
                        )
                        
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 16.dp),
                            color = Color.LightGray.copy(alpha = 0.3f)
                        )
                        
                        TenorSliderCard(
                            tenor = uiState.tenor,
                            minTenor = uiState.minTenor,
                            maxTenor = uiState.maxTenor,
                            onTenorChange = { viewModel.updateTenor(it) },
                            error = uiState.fieldErrors?.get("tenor")
                        )
                    }
                }
                
                // CARD 3: DATA PRIBADI & KEUANGAN
                FormCard(title = "Data Pekerjaan & Keuangan") {
                    LoanTextField(
                        value = uiState.occupation,
                        onValueChange = { viewModel.updateOccupation(it) },
                        label = "Pekerjaan *",
                        icon = Icons.Filled.Work,
                        error = uiState.fieldErrors?.get("occupation")
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LoanTextField(
                        value = uiState.companyName,
                        onValueChange = { viewModel.updateCompanyName(it) },
                        label = "Nama Perusahaan (Optional)",
                        icon = Icons.Filled.Business
                    )
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = Color.LightGray.copy(alpha = 0.3f)
                    )
                    
                    LoanTextField(
                        value = uiState.rekeningNumber,
                        onValueChange = { viewModel.updateRekeningNumber(it) },
                        label = "Nomor Rekening *",
                        icon = Icons.Filled.CreditCard,
                        keyboardType = KeyboardType.Number,
                        error = uiState.fieldErrors?.get("rekeningNumber")
                    )
                }
                
                // CARD 4: DOKUMEN PENDUKUNG
                FormCard(title = "Upload Dokumen") {
                    FileUploadRow(
                        label = "Cover Buku Tabungan *",
                        uri = uiState.savingBookCoverUri,
                        error = uiState.fieldErrors?.get("savingBookCover"),
                        onClick = { openImageSelection("savingBook") }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    FileUploadRow(
                        label = "Slip Gaji *",
                        uri = uiState.payslipPhotoUri,
                        error = uiState.fieldErrors?.get("payslipPhoto"),
                        onClick = { openImageSelection("payslip") }
                    )
                }
                
                // CARD 5: PREVIEW PENGAJUAN
                if (uiState.selectedPlafondId != null && uiState.amount.isNotBlank()) {
                    LoanPreviewCard(
                        amount = uiState.amount,
                        tenor = uiState.tenor,
                        interestRate = uiState.interestRate,
                        plafondName = uiState.activePlafond?.plafondName ?: ""
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Submit Button
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 8.dp,
                    color = if (uiState.isSubmitting) Color.LightGray else LoanPrimaryColor,
                    onClick = {
                        if (!uiState.isSubmitting) {
                            viewModel.submitLoanApplication(
                                savingBookCoverFile,
                                payslipPhotoFile
                            )
                        }
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(
                                if (!uiState.isSubmitting) {
                                    Modifier.background(
                                        Brush.horizontalGradient(
                                            colors = listOf(LoanPrimaryColor, LoanSecondaryColor)
                                        )
                                    )
                                } else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Ajukan Pinjaman Sekarang", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

/**
 * Banner untuk menampilkan status offline queued.
 * Ditampilkan ketika pengajuan disimpan secara offline dan menunggu sinkronisasi.
 */
@Composable
private fun OfflineQueuedBanner() {
    Surface(
        color = Color(0xFFFFF3E0), // Orange background
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFFF9800).copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with animation effect
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF9800).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.CloudQueue,
                    contentDescription = null,
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Pengajuan Tersimpan Offline",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFFE65100)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Data pengajuan akan dikirim otomatis saat koneksi tersedia.",
                    fontSize = 12.sp,
                    color = Color(0xFF6D4C41),
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun LocationStatusCard(
    latitude: Double?,
    longitude: Double?,
    error: String?,
    onRefresh: () -> Unit
) {
    val statusColor = if (latitude != null) Color(0xFF4CAF50) else if (error != null) Color.Red else Color(0xFFFF9800)
    val bgColor = statusColor.copy(alpha = 0.05f)
    
    // UI refined to be part of a larger card
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(BorderStroke(1.dp, statusColor.copy(alpha = 0.2f)), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(statusColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (latitude != null) Icons.Filled.LocationOn else Icons.Filled.LocationOff,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                if (latitude != null) "Lokasi Terdeteksi" else if (error != null) "Gagal Mendeteksi Lokasi" else "Mendeteksi Lokasi...",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color.Black.copy(alpha = 0.8f)
            )
            if (latitude != null && longitude != null) {
                Text(
                    "${String.format("%.6f", latitude)}, ${String.format("%.6f", longitude)}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            } else if (error != null) {
                Text(
                    "# $error",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        TextButton(
            onClick = onRefresh,
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.height(32.dp),
            colors = ButtonDefaults.textButtonColors(contentColor = statusColor)
        ) {
            Text("Refresh", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> DropdownCard(
    label: String,
    options: List<Pair<T, String>>,
    selectedId: T?,
    onSelect: (T) -> Unit,
    error: String? = null
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedText = options.find { it.first == selectedId }?.second ?: ""
    
    Column {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (error != null) MaterialTheme.colorScheme.error else Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedText,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                placeholder = { Text("Pilih $label", fontSize = 14.sp) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LoanSecondaryColor,
                    unfocusedBorderColor = LoanSecondaryColor.copy(alpha = 0.4f),
                    focusedLabelColor = LoanPrimaryColor,
                    errorBorderColor = MaterialTheme.colorScheme.error
                ),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp),
                isError = error != null,
                supportingText = {
                    if (error != null) {
                        Text(
                            text = "# $error",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.second, fontSize = 14.sp) },
                        onClick = {
                            onSelect(option.first)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AmountSliderCard(
    amount: String,
    minAmount: BigDecimal,
    maxAmount: BigDecimal,
    onAmountChange: (String) -> Unit,
    onSliderChange: (Float) -> Unit,
    error: String? = null
) {
    val currentAmount = try { BigDecimal(amount) } catch (e: Exception) { minAmount }
    val sliderPosition = if (maxAmount > minAmount) {
        currentAmount.subtract(minAmount).divide(
            maxAmount.subtract(minAmount),
            2,
            java.math.RoundingMode.HALF_UP
        ).toFloat().coerceIn(0f, 1f)
    } else 0f
    
    Column {
        Text("Jumlah Pinjaman", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (error != null) MaterialTheme.colorScheme.error else Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        // Amount display
        Text(
            formatCurrency(currentAmount),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = if (error != null) MaterialTheme.colorScheme.error else LoanPrimaryColor
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Slider
        Slider(
            value = sliderPosition,
            onValueChange = { onSliderChange(it) },
            colors = SliderDefaults.colors(
                thumbColor = if (error != null) MaterialTheme.colorScheme.error else LoanSecondaryColor,
                activeTrackColor = if (error != null) MaterialTheme.colorScheme.error else LoanSecondaryColor
            )
        )
        
        // Min/Max labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(formatCurrency(minAmount), fontSize = 11.sp, color = Color.Gray)
            Text(formatCurrency(maxAmount), fontSize = 11.sp, color = Color.Gray)
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Manual input
        OutlinedTextField(
            value = amount,
            onValueChange = onAmountChange,
            label = { Text("Atau input manual", fontSize = 14.sp) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = LoanSecondaryColor,
                unfocusedBorderColor = LoanSecondaryColor.copy(alpha = 0.4f),
                focusedLabelColor = LoanPrimaryColor,
                cursorColor = LoanSecondaryColor,
                errorBorderColor = MaterialTheme.colorScheme.error,
                errorLabelColor = MaterialTheme.colorScheme.error
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp),
            isError = error != null,
            supportingText = {
                if (error != null) {
                    Text(
                        text = "# $error",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
}

@Composable
private fun TenorSliderCard(
    tenor: Int,
    minTenor: Int,
    maxTenor: Int,
    onTenorChange: (Int) -> Unit,
    error: String? = null
) {
    val sliderPosition = if (maxTenor > minTenor) {
        (tenor - minTenor).toFloat() / (maxTenor - minTenor).toFloat()
    } else 0f
    
    Column {
        Text("Tenor (Bulan)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (error != null) MaterialTheme.colorScheme.error else Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        // Tenor display
        Text(
            "$tenor Bulan",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = if (error != null) MaterialTheme.colorScheme.error else LoanPrimaryColor
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Slider
        Slider(
            value = sliderPosition,
            onValueChange = { 
                val newTenor = minTenor + ((maxTenor - minTenor) * it).toInt()
                onTenorChange(newTenor)
            },
            colors = SliderDefaults.colors(
                thumbColor = if (error != null) MaterialTheme.colorScheme.error else LoanSecondaryColor,
                activeTrackColor = if (error != null) MaterialTheme.colorScheme.error else LoanSecondaryColor
            )
        )
        
        // Min/Max labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("$minTenor bulan", fontSize = 11.sp, color = Color.Gray)
            Text("$maxTenor bulan", fontSize = 11.sp, color = Color.Gray)
        }

        if (error != null) {
            Text(
                text = "# $error",
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun FormCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Gradient Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(LoanPrimaryColor, LoanSecondaryColor)
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
            
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun LoanTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    error: String? = null
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, fontSize = 14.sp) }, // Smaller font
            leadingIcon = { Icon(icon, contentDescription = label, tint = if (error != null) MaterialTheme.colorScheme.error else LoanSecondaryColor) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = LoanSecondaryColor,
                unfocusedBorderColor = LoanSecondaryColor.copy(alpha = 0.4f),
                focusedLabelColor = LoanPrimaryColor,
                cursorColor = LoanSecondaryColor,
                errorBorderColor = MaterialTheme.colorScheme.error,
                errorLabelColor = MaterialTheme.colorScheme.error
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp),
            isError = error != null,
            supportingText = {
                if (error != null) {
                    Text(
                        text = "# $error",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
}

@Composable
private fun FileUploadRow(
    label: String,
    uri: Uri?,
    error: String? = null,
    onClick: () -> Unit
) {
    val borderColor = if (uri != null) Color(0xFF10B981) else if (error != null) MaterialTheme.colorScheme.error else LoanSecondaryColor.copy(alpha = 0.5f)
    val backgroundColor = if (uri != null) Color(0xFFECFDF5) else if (error != null) Color(0xFFFDE8E8) else LoanSecondaryColor.copy(alpha = 0.05f)
    
    // Dashed border effect for empty state
    val stroke = androidx.compose.ui.graphics.drawscope.Stroke(
        width = 2f,
        pathEffect = if (uri == null) androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f) else null
    )

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(backgroundColor)
                .clickable { onClick() }
                .drawBehind {
                    drawRoundRect(
                        color = borderColor,
                        style = stroke,
                        cornerRadius = CornerRadius(16.dp.toPx())
                    )
                }
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Upload,
                        contentDescription = "Upload",
                        tint = if (uri != null) Color(0xFF10B981) else if (error != null) MaterialTheme.colorScheme.error else LoanSecondaryColor
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = LoanPrimaryColor)
                    Spacer(modifier = Modifier.height(2.dp))
                    if (uri != null) {
                        Text("File berhasil dipilih ✓", fontSize = 12.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                    } else if (error != null) {
                        Text("# $error", fontSize = 12.sp, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    } else {
                        Text("Tap untuk memilih dokumen (JPG/PNG)", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

// Helper functions
/**
 * Get location dengan fallback strategy untuk mendukung offline mode:
 * 1. Coba getCurrentLocation (GPS fresh) - butuh waktu tapi akurat
 * 2. Jika gagal/null, fallback ke lastLocation (cached) - instant tapi mungkin stale
 * 3. Jika keduanya gagal, panggil onFailure callback
 */
private fun getLocation(
    context: Context,
    fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient,
    onLocationReceived: (Double, Double) -> Unit,
    onFailure: ((String) -> Unit)? = null
) {
    try {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) 
            == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) 
            == PackageManager.PERMISSION_GRANTED
        ) {
            // Strategy 1: Try to get fresh GPS location
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        onLocationReceived(location.latitude, location.longitude)
                    } else {
                        // Strategy 2: Fallback to last known location (works offline!)
                        getLastKnownLocation(context, fusedLocationClient, onLocationReceived, onFailure)
                    }
                }
                .addOnFailureListener { e ->
                    // GPS failed, try last known location
                    getLastKnownLocation(context, fusedLocationClient, onLocationReceived, onFailure)
                }
        } else {
            onFailure?.invoke("Izin lokasi belum diberikan")
        }
    } catch (e: SecurityException) {
        e.printStackTrace()
        onFailure?.invoke("Error keamanan: ${e.message}")
    }
}

/**
 * Get last known location dari cache sistem.
 * Ini bekerja OFFLINE karena menggunakan data yang sudah tersimpan di device.
 */
private fun getLastKnownLocation(
    context: Context,
    fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient,
    onLocationReceived: (Double, Double) -> Unit,
    onFailure: ((String) -> Unit)? = null
) {
    try {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) 
            == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) 
            == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        onLocationReceived(location.latitude, location.longitude)
                    } else {
                        onFailure?.invoke("Lokasi tidak tersedia. Pastikan GPS aktif dan coba di area terbuka.")
                    }
                }
                .addOnFailureListener { e ->
                    onFailure?.invoke("Gagal mendapatkan lokasi: ${e.message}")
                }
        }
    } catch (e: SecurityException) {
        e.printStackTrace()
        onFailure?.invoke("Error keamanan: ${e.message}")
    }
}

private fun uriToFile(context: Context, uri: Uri): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, "temp_${System.currentTimeMillis()}.jpg")
        inputStream?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        file
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun formatCurrency(amount: BigDecimal): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return format.format(amount)
}

@Composable
private fun ActivePlafondCard(
    plafondName: String,
    remainingAmount: BigDecimal
) {
    val themeColor = getPlafondColor(plafondName)
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(themeColor.copy(alpha = 0.05f))
            .border(BorderStroke(1.dp, themeColor.copy(alpha = 0.2f)), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text("Plafond Aktif", style = MaterialTheme.typography.labelSmall, color = themeColor, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(plafondName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
             Text("Sisa Limit", fontSize = 11.sp, color = Color.Gray)
             Text(
                 formatCurrency(remainingAmount),
                 fontWeight = FontWeight.Bold,
                 color = themeColor,
                 fontSize = 14.sp
             )
        }
    }
}

private fun getPlafondColor(name: String): Color {
    return when {
        name.contains("Gold", ignoreCase = true) -> Color(0xFFFFC107) // Amber 500
        name.contains("Silver", ignoreCase = true) -> Color(0xFF9E9E9E) // Grey 500
        name.contains("Bronze", ignoreCase = true) -> Color(0xFFD84315) // Deep Orange 800
        name.contains("Platinum", ignoreCase = true) -> Color(0xFF00BCD4) // Cyan 500
        name.contains("Red", ignoreCase = true) -> Color(0xFFF44336) // Red 500
        name.contains("Black", ignoreCase = true) -> Color(0xFF212121) // Grey 900
        else -> LoanovaBlue
    }
}

/**
 * Loan Preview Card - Menampilkan ringkasan perhitungan pinjaman sebelum submit.
 * Menggunakan metode Flat Interest (Bunga Flat).
 * Design: Modern fintech style dengan glassmorphism effect
 */
@Composable
private fun LoanPreviewCard(
    amount: String,
    tenor: Int,
    interestRate: BigDecimal,
    plafondName: String
) {
    // Parse amount
    val loanAmount = try { 
        BigDecimal(amount.replace(",", "").replace(".", "")) 
    } catch (e: Exception) { 
        BigDecimal.ZERO 
    }
    
    // Perhitungan Flat Interest
    // Total Bunga = Pokok × (Bunga% / 100) × Tenor
    val totalInterest = loanAmount
        .multiply(interestRate)
        .divide(BigDecimal("100"), 2, RoundingMode.HALF_UP)
        .multiply(BigDecimal(tenor))
    
    // Total Pelunasan = Pokok + Total Bunga
    val totalRepayment = loanAmount.add(totalInterest)
    
    // Cicilan per Bulan = Total Pelunasan / Tenor
    val monthlyInstallment = if (tenor > 0) {
        totalRepayment.divide(BigDecimal(tenor), 0, RoundingMode.CEILING)
    } else {
        BigDecimal.ZERO
    }
    
    val themeColor = getPlafondColor(plafondName)
    val gradientColors = listOf(
        themeColor,
        themeColor.copy(alpha = 0.85f),
        themeColor.copy(alpha = 0.7f)
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = themeColor.copy(alpha = 0.4f),
                spotColor = themeColor.copy(alpha = 0.4f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = gradientColors,
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
        ) {
            // Decorative circles for modern look
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .offset(x = (-30).dp, y = (-30).dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
            )
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 20.dp, y = (-20).dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
            )
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 15.dp, y = 15.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
            )
            
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("📋", fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Preview Pengajuan",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "Simulasi perhitungan pinjaman Anda",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                    // Badge plafond
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                    ) {
                        Text(
                            plafondName,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
                
                // Main Content Card (Glassmorphism style)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                        .padding(bottom = 10.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White.copy(alpha = 0.95f),
                    shadowElevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Row 1: Jumlah Pinjaman & Tenor
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ModernInfoBox(
                                modifier = Modifier.weight(1f),
                                icon = "💰",
                                label = "Jumlah Pinjaman",
                                value = formatCurrency(loanAmount),
                                accentColor = LoanPrimaryColor
                            )
                            ModernInfoBox(
                                modifier = Modifier.weight(1f),
                                icon = "📅",
                                label = "Tenor",
                                value = "$tenor Bulan",
                                accentColor = LoanPrimaryColor
                            )
                        }
                        
                        // Row 2: Bunga per Bulan & Total Bunga
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ModernInfoBox(
                                modifier = Modifier.weight(1f),
                                icon = "📊",
                                label = "Bunga/Bulan",
                                value = "${interestRate.setScale(2, RoundingMode.HALF_UP)}%",
                                accentColor = Color(0xFFFF9800)
                            )
                            ModernInfoBox(
                                modifier = Modifier.weight(1f),
                                icon = "📈",
                                label = "Total Bunga",
                                value = formatCurrency(totalInterest),
                                accentColor = Color(0xFFFF9800)
                            )
                        }
                        
                        // Stylish Divider
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(2.dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                themeColor.copy(alpha = 0.3f)
                                            )
                                        )
                                    )
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = themeColor.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    "TOTAL",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    fontSize = 10.sp,
                                    color = themeColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(2.dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                themeColor.copy(alpha = 0.3f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )
                        }
                        
                        // Total Pelunasan - Premium Highlight
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            themeColor.copy(alpha = 0.08f),
                                            themeColor.copy(alpha = 0.15f),
                                            themeColor.copy(alpha = 0.08f)
                                        )
                                    )
                                )
                                .border(
                                    BorderStroke(1.dp, themeColor.copy(alpha = 0.2f)),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("💳", fontSize = 12.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            "Total Pelunasan",
                                            fontSize = 10.sp,
                                            color = Color.Gray,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        formatCurrency(totalRepayment),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = themeColor
                                    )
                                }
                                
                                // Cicilan per bulan - Modern badge
                                Column(
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        "Cicilan/Bulan",
                                        fontSize = 9.sp,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                Brush.horizontalGradient(
                                                    colors = listOf(
                                                        Color(0xFF4CAF50),
                                                        Color(0xFF66BB6A)
                                                    )
                                                )
                                            )
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            formatCurrency(monthlyInstallment),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Modern Info disclaimer
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFFFFF8E1),
                                            Color(0xFFFFF3E0)
                                        )
                                    )
                                )
                                .border(
                                    BorderStroke(1.dp, Color(0xFFFFE082).copy(alpha = 0.5f)),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFB74D).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("ℹ", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Perhitungan menggunakan metode bunga flat. Nilai akhir dapat berbeda sesuai kebijakan.",
                                fontSize = 9.sp,
                                color = Color(0xFF6D4C41),
                                lineHeight = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModernInfoBox(
    modifier: Modifier = Modifier,
    icon: String,
    label: String,
    value: String,
    accentColor: Color
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.05f),
                        accentColor.copy(alpha = 0.1f)
                    )
                )
            )
            .border(
                BorderStroke(1.dp, accentColor.copy(alpha = 0.15f)),
                RoundedCornerShape(10.dp)
            )
            .padding(10.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(accentColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(icon, fontSize = 10.sp)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    label,
                    fontSize = 9.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }
    }
}
/**
 * Dialog untuk memilih sumber gambar (Kamera atau Galeri).
 * 
 * Menampilkan popup dengan 2 pilihan:
 * - Kamera: Membuka aplikasi kamera bawaan HP untuk mengambil foto langsung.
 * - Galeri: Membuka galeri foto untuk memilih gambar yang sudah ada.
 */
@Composable
private fun ImageSourceOptionDialog(
    onDismiss: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.padding(16.dp),
            elevation = CardDefaults.cardElevation(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Pilih Sumber Gambar",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = LoanPrimaryColor
                )
                Spacer(modifier = Modifier.height(28.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Camera Option
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onCameraClick() }
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = LoanSecondaryColor.copy(alpha = 0.15f),
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Kamera",
                                    tint = LoanSecondaryColor,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Kamera", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = LoanPrimaryColor)
                    }

                    // Gallery Option
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onGalleryClick() }
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = LoanAccentColor.copy(alpha = 0.15f),
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = "Galeri",
                                    tint = LoanSecondaryColor,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Galeri", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = LoanPrimaryColor)
                    }
                }
                
                Spacer(modifier = Modifier.height(28.dp))
                TextButton(onClick = onDismiss) {
                    Text("Batal", color = Color.Gray, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}