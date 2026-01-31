package com.example.loanova_android.ui.features.loan

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.loanova_android.ui.theme.*
import com.example.loanova_android.data.model.dto.UserPlafondResponse
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.io.File
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

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
            getLocation(context, fusedLocationClient) { lat, lng ->
                viewModel.updateLocation(lat, lng)
            }
        } else {
            viewModel.setLocationError("Izin lokasi diperlukan untuk mengajukan pinjaman")
        }
    }
    
    // File picker for saving book cover
    var savingBookCoverFile by remember { mutableStateOf<File?>(null) }
    var payslipPhotoFile by remember { mutableStateOf<File?>(null) }
    
    val savingBookLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { 
            viewModel.updateSavingBookCover(it)
            savingBookCoverFile = uriToFile(context, it)
        }
    }
    
    val payslipLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { 
            viewModel.updatePayslipPhoto(it)
            payslipPhotoFile = uriToFile(context, it)
        }
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
            getLocation(context, fusedLocationClient) { lat, lng ->
                viewModel.updateLocation(lat, lng)
            }
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
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
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
                    containerColor = LoanovaBlue,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(LoanovaBlue.copy(alpha = 0.05f), Color.White)
                    )
                )
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Location Status Card
                LocationStatusCard(
                    latitude = uiState.latitude,
                    longitude = uiState.longitude,
                    error = uiState.locationError,
                    onRefresh = {
                        getLocation(context, fusedLocationClient) { lat, lng ->
                            viewModel.updateLocation(lat, lng)
                        }
                    }
                )
                
                // Branch Dropdown
                if (uiState.isLoadingBranches) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    DropdownCard(
                        label = "Pilih Cabang",
                        options = uiState.branches.map { it.id to "${it.branchCode} - ${it.branchName}" },
                        selectedId = uiState.selectedBranchId,
                        onSelect = { viewModel.selectBranch(it) }
                    )
                }
                
                // Active Plafond Info
                if (uiState.isLoadingPlafonds) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else if (uiState.activePlafond != null) {
                    ActivePlafondCard(
                        plafondName = uiState.activePlafond!!.plafondName,
                        remainingAmount = uiState.activePlafond!!.remainingAmount
                    )
                }
                
                // Amount Section with Slider
                if (uiState.selectedPlafondId != null) {
                    AmountSliderCard(
                        amount = uiState.amount,
                        minAmount = uiState.minAmount,
                        maxAmount = uiState.maxAmount,
                        onAmountChange = { viewModel.updateAmount(it) },
                        onSliderChange = { viewModel.updateAmountFromSlider(it) }
                    )
                    
                    // Tenor Section with Slider
                    TenorSliderCard(
                        tenor = uiState.tenor,
                        minTenor = uiState.minTenor,
                        maxTenor = uiState.maxTenor,
                        onTenorChange = { viewModel.updateTenor(it) }
                    )
                }
                
                // Form Inputs
                FormCard(title = "Data Pekerjaan") {
                    LoanTextField(
                        value = uiState.occupation,
                        onValueChange = { viewModel.updateOccupation(it) },
                        label = "Pekerjaan *",
                        icon = Icons.Filled.Work
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LoanTextField(
                        value = uiState.companyName,
                        onValueChange = { viewModel.updateCompanyName(it) },
                        label = "Nama Perusahaan (Optional)",
                        icon = Icons.Filled.Business
                    )
                }
                
                FormCard(title = "Data Keuangan") {
                    LoanTextField(
                        value = uiState.rekeningNumber,
                        onValueChange = { viewModel.updateRekeningNumber(it) },
                        label = "Nomor Rekening *",
                        icon = Icons.Filled.CreditCard,
                        keyboardType = KeyboardType.Number
                    )
                }
                
                // Document Uploads
                FormCard(title = "Upload Dokumen") {
                    FileUploadRow(
                        label = "Cover Buku Tabungan *",
                        uri = uiState.savingBookCoverUri,
                        onClick = { savingBookLauncher.launch("image/*") }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    FileUploadRow(
                        label = "Slip Gaji *",
                        uri = uiState.payslipPhotoUri,
                        onClick = { payslipLauncher.launch("image/*") }
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Submit Button
                Button(
                    onClick = {
                        if (savingBookCoverFile != null && payslipPhotoFile != null) {
                            viewModel.submitLoanApplication(
                                savingBookCoverFile!!,
                                payslipPhotoFile!!
                            )
                        } else {
                            Toast.makeText(context, "Lengkapi semua dokumen", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !uiState.isSubmitting,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LoanovaBlue
                    )
                ) {
                    if (uiState.isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Text("Ajukan Pinjaman", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (latitude != null) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.LocationOn,
                contentDescription = "Location",
                tint = if (latitude != null) Color(0xFF4CAF50) else Color(0xFFFF9800)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (latitude != null) "Lokasi Terdeteksi" else "Mendeteksi Lokasi...",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                if (latitude != null && longitude != null) {
                    Text(
                        "Lat: ${String.format("%.6f", latitude)}, Lng: ${String.format("%.6f", longitude)}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                error?.let {
                    Text(it, fontSize = 12.sp, color = Color.Red)
                }
            }
            TextButton(onClick = onRefresh) {
                Text("Refresh")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> DropdownCard(
    label: String,
    options: List<Pair<T, String>>,
    selectedId: T?,
    onSelect: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedText = options.find { it.first == selectedId }?.second ?: ""
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            
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
                    placeholder = { Text("Pilih $label") }
                )
                
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.second) },
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
}

@Composable
private fun AmountSliderCard(
    amount: String,
    minAmount: BigDecimal,
    maxAmount: BigDecimal,
    onAmountChange: (String) -> Unit,
    onSliderChange: (Float) -> Unit
) {
    val currentAmount = try { BigDecimal(amount) } catch (e: Exception) { minAmount }
    val sliderPosition = if (maxAmount > minAmount) {
        currentAmount.subtract(minAmount).divide(
            maxAmount.subtract(minAmount),
            2,
            java.math.RoundingMode.HALF_UP
        ).toFloat().coerceIn(0f, 1f)
    } else 0f
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Jumlah Pinjaman", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            
            // Amount display
            Text(
                formatCurrency(currentAmount),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = LoanovaBlue
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Slider
            Slider(
                value = sliderPosition,
                onValueChange = { onSliderChange(it) },
                colors = SliderDefaults.colors(
                    thumbColor = LoanovaBlue,
                    activeTrackColor = LoanovaBlue
                )
            )
            
            // Min/Max labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(formatCurrency(minAmount), fontSize = 12.sp, color = Color.Gray)
                Text(formatCurrency(maxAmount), fontSize = 12.sp, color = Color.Gray)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Manual input
            OutlinedTextField(
                value = amount,
                onValueChange = onAmountChange,
                label = { Text("Atau input manual") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
    }
}

@Composable
private fun TenorSliderCard(
    tenor: Int,
    minTenor: Int,
    maxTenor: Int,
    onTenorChange: (Int) -> Unit
) {
    val sliderPosition = if (maxTenor > minTenor) {
        (tenor - minTenor).toFloat() / (maxTenor - minTenor).toFloat()
    } else 0f
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Tenor (Bulan)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            
            // Tenor display
            Text(
                "$tenor Bulan",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = LoanovaBlue
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
                    thumbColor = LoanovaBlue,
                    activeTrackColor = LoanovaBlue
                )
            )
            
            // Min/Max labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("$minTenor bulan", fontSize = 12.sp, color = Color.Gray)
                Text("$maxTenor bulan", fontSize = 12.sp, color = Color.Gray)
            }
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
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun LoanTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = label, tint = LoanovaBlue) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}

@Composable
private fun FileUploadRow(
    label: String,
    uri: Uri?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Filled.Upload,
            contentDescription = "Upload",
            tint = if (uri != null) Color(0xFF4CAF50) else LoanovaBlue
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 14.sp)
            if (uri != null) {
                Text("File terpilih ✓", fontSize = 12.sp, color = Color(0xFF4CAF50))
            } else {
                Text("Tap untuk memilih file", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

// Helper functions
private fun getLocation(
    context: Context,
    fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient,
    onLocationReceived: (Double, Double) -> Unit
) {
    try {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) 
            == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) 
            == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    location?.let {
                        onLocationReceived(it.latitude, it.longitude)
                    }
                }
        }
    } catch (e: SecurityException) {
        e.printStackTrace()
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Plafond Aktif", style = MaterialTheme.typography.labelMedium, color = LoanovaBlue)
            Spacer(modifier = Modifier.height(4.dp))
            Text(plafondName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            
            HorizontalDivider(color = LoanovaBlue.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                 Text("Sisa Limit Tersedia", fontSize = 12.sp, color = Color.Gray)
                 Text(
                     formatCurrency(remainingAmount),
                     fontWeight = FontWeight.Bold,
                     color = LoanovaBlue,
                     fontSize = 16.sp
                 )
            }
        }
    }
}
