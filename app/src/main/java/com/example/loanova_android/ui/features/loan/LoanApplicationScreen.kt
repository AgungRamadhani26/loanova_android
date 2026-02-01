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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
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
    // Success message logic remains as Toast
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
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
                            getLocation(context, fusedLocationClient) { lat, lng ->
                                viewModel.updateLocation(lat, lng)
                            }
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
                        onClick = { savingBookLauncher.launch("image/*") }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    FileUploadRow(
                        label = "Slip Gaji *",
                        uri = uiState.payslipPhotoUri,
                        error = uiState.fieldErrors?.get("payslipPhoto"),
                        onClick = { payslipLauncher.launch("image/*") }
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Submit Button
                Button(
                    onClick = {
                        viewModel.submitLoanApplication(
                            savingBookCoverFile,
                            payslipPhotoFile
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp)),
                    enabled = !uiState.isSubmitting,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LoanovaBlue,
                        contentColor = Color.White,
                        disabledContainerColor = Color.LightGray
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(
                                if (!uiState.isSubmitting) {
                                    Modifier.background(
                                        Brush.horizontalGradient(
                                            colors = listOf(LoanovaBlue, LoanovaLightBlue)
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
                            Text("Ajukan Pinjaman Sekarang", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                        }
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
                    focusedBorderColor = LoanovaBlue,
                    unfocusedBorderColor = LoanovaBlue.copy(alpha = 0.4f),
                    focusedLabelColor = LoanovaBlue,
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
            color = if (error != null) MaterialTheme.colorScheme.error else LoanovaBlue
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Slider
        Slider(
            value = sliderPosition,
            onValueChange = { onSliderChange(it) },
            colors = SliderDefaults.colors(
                thumbColor = if (error != null) MaterialTheme.colorScheme.error else LoanovaBlue,
                activeTrackColor = if (error != null) MaterialTheme.colorScheme.error else LoanovaBlue
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
                focusedBorderColor = LoanovaBlue,
                unfocusedBorderColor = LoanovaBlue.copy(alpha = 0.4f),
                focusedLabelColor = LoanovaBlue,
                cursorColor = LoanovaBlue,
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
            color = if (error != null) MaterialTheme.colorScheme.error else LoanovaBlue
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
                thumbColor = if (error != null) MaterialTheme.colorScheme.error else LoanovaBlue,
                activeTrackColor = if (error != null) MaterialTheme.colorScheme.error else LoanovaBlue
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Gradient Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(LoanovaBlue, LoanovaLightBlue)
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
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
            leadingIcon = { Icon(icon, contentDescription = label, tint = if (error != null) MaterialTheme.colorScheme.error else LoanovaBlue) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = LoanovaBlue,
                unfocusedBorderColor = LoanovaBlue.copy(alpha = 0.4f),
                focusedLabelColor = LoanovaBlue,
                cursorColor = LoanovaBlue,
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
    val borderColor = if (uri != null) Color(0xFF4CAF50) else if (error != null) MaterialTheme.colorScheme.error else LoanovaBlue.copy(alpha = 0.5f)
    val backgroundColor = if (uri != null) Color(0xFFE8F5E9) else if (error != null) Color(0xFFFDE8E8) else LoanovaBlue.copy(alpha = 0.05f)
    
    // Dashed border effect for empty state
    val stroke = androidx.compose.ui.graphics.drawscope.Stroke(
        width = 2f,
        pathEffect = if (uri == null) androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f) else null
    )

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(backgroundColor)
                .clickable { onClick() }
                .drawBehind {
                    drawRoundRect(
                        color = borderColor,
                        style = stroke,
                        cornerRadius = CornerRadius(12.dp.toPx())
                    )
                }
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Upload,
                        contentDescription = "Upload",
                        tint = if (uri != null) Color(0xFF4CAF50) else if (error != null) MaterialTheme.colorScheme.error else LoanovaBlue
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Black.copy(alpha=0.8f))
                    Spacer(modifier = Modifier.height(2.dp))
                    if (uri != null) {
                        Text("File berhasil dipilih ✓", fontSize = 12.sp, color = Color(0xFF4CAF50))
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
