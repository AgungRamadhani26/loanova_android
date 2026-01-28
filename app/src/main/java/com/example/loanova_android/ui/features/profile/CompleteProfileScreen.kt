package com.example.loanova_android.ui.features.profile

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.loanova_android.R
import com.example.loanova_android.ui.theme.LoanovaBlue
import com.example.loanova_android.ui.theme.LoanovaLightBlue
import com.example.loanova_android.ui.theme.LoanovaBackground
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompleteProfileScreen(
    onNavigateBack: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: CompleteProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // Form States
    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var nik by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") } 
    var npwpNumber by remember { mutableStateOf("") }

    // File States
    var ktpUri by remember { mutableStateOf<Uri?>(null) }
    var profileUri by remember { mutableStateOf<Uri?>(null) }
    var npwpUri by remember { mutableStateOf<Uri?>(null) }

    // Launchers
    val ktpLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? -> uri?.let { ktpUri = it; viewModel.clearError() } }
    val profileLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? -> uri?.let { profileUri = it; viewModel.clearError() } }
    val npwpLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? -> uri?.let { npwpUri = it; viewModel.clearError() } }

    LaunchedEffect(uiState.success) {
        if (uiState.success != null) {
            Toast.makeText(context, "Profil Berhasil Dilengkapi!", Toast.LENGTH_SHORT).show()
            onSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LoanovaBackground)
    ) {
        // Gradient Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(LoanovaBlue, LoanovaLightBlue)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, start = 16.dp, end = 16.dp)
            ) {
                // Back Button
                IconButton(
                    onClick = onNavigateBack,
                    colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Lengkapi Profil",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    modifier = Modifier.padding(start = 12.dp)
                )
                Text(
                    text = "Isi data diri Anda dengan benar untuk verifikasi akun.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White.copy(alpha = 0.9f)
                    ),
                    modifier = Modifier.padding(start = 12.dp, top = 4.dp)
                )
            }
        }

        // Main Content Card
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 220.dp),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // Global Error Message
                // Global Error Message
                androidx.compose.animation.AnimatedVisibility(
                    visible = uiState.error != null,
                    enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
                    exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically()
                ) {
                    Surface(
                        color = Color(0xFFFDE8E8), // Pink background
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.2f)), // Subtle red border
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = Color(0xFFEF4444), // Red color
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = uiState.error ?: "",
                                color = Color(0xFFEF4444),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Start
                            )
                        }
                    }
                }

                LoanovaTextField(
                    value = fullName,
                    onValueChange = { fullName = it; viewModel.clearError() },
                    label = "Nama Lengkap Sesuai KTP",
                    icon = Icons.Outlined.Person,
                    error = uiState.fieldErrors?.get("fullName")
                )

                LoanovaTextField(
                    value = phoneNumber,
                    onValueChange = { input -> 
                        if (input.all { it.isDigit() }) {
                            phoneNumber = input
                            viewModel.clearError() 
                        }
                    },
                    label = "Nomor Telepon Aktif",
                    icon = Icons.Outlined.Phone,
                    error = uiState.fieldErrors?.get("phoneNumber"),
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                )

                LoanovaTextField(
                    value = address,
                    onValueChange = { address = it; viewModel.clearError() },
                    label = "Alamat Lengkap",
                    icon = Icons.Outlined.Home,
                    error = uiState.fieldErrors?.get("userAddress")
                )

                LoanovaTextField(
                    value = nik,
                    onValueChange = { input -> 
                        if (input.all { it.isDigit() }) {
                           nik = input
                           viewModel.clearError() 
                        }
                    },
                    label = "NIK (16 Digit)",
                    icon = Icons.Outlined.CreditCard,
                    error = uiState.fieldErrors?.get("nik"),
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                )

                // Date Picker
                var openDatePicker by remember { mutableStateOf(false) }
                
                if (openDatePicker) {
                    val datePickerState = rememberDatePickerState()
                    DatePickerDialog(
                        onDismissRequest = { openDatePicker = false },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    datePickerState.selectedDateMillis?.let { millis ->
                                        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                                        birthDate = sdf.format(java.util.Date(millis))
                                        viewModel.clearError()
                                    }
                                    openDatePicker = false
                                }
                            ) {
                                Text("OK")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    openDatePicker = false
                                }
                            ) {
                                Text("Batal")
                            }
                        }
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }

                // Birth Date Field (Clickable)
                Box(modifier = Modifier.clickable { openDatePicker = true }) {
                    LoanovaTextField(
                        value = birthDate,
                        onValueChange = {}, // Read only
                        label = "Tanggal Lahir (YYYY-MM-DD)",
                        icon = Icons.Outlined.CalendarToday,
                        error = uiState.fieldErrors?.get("birthDate"),
                        readOnly = true
                    )
                    // Transparent overlay to catch clicks
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { openDatePicker = true }
                    )
                }
                
                LoanovaTextField(
                    value = npwpNumber,
                    onValueChange = { input -> 
                         if (input.all { it.isDigit() }) {
                            npwpNumber = input
                            viewModel.clearError() 
                         }
                    },
                    label = "Nomor NPWP (Opsional)",
                    icon = Icons.Outlined.Description,
                    error = uiState.fieldErrors?.get("npwpNumber"),
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                )

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Dokumen Pendukung",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = LoanovaBlue,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(12.dp))

                FileUploadRow(
                    label = "Foto Profil (Wajib)", 
                    uri = profileUri, 
                    error = uiState.fieldErrors?.get("profilePhoto"),
                    onClick = { profileLauncher.launch("image/*") }
                )
                FileUploadRow(
                    label = "Foto KTP (Wajib)", 
                    uri = ktpUri, 
                    error = uiState.fieldErrors?.get("ktpPhoto"),
                    onClick = { ktpLauncher.launch("image/*") }
                )
                FileUploadRow(
                    label = "Foto NPWP (Opsional)", 
                    uri = npwpUri, 
                    error = uiState.fieldErrors?.get("npwpPhoto"),
                    onClick = { npwpLauncher.launch("image/*") }
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                         val ktpFile = ktpUri?.let { uriToFile(context, it) }
                         val profileFile = profileUri?.let { uriToFile(context, it) }
                         val npwpFile = npwpUri?.let { uriToFile(context, it) }
                         
                         // Allow sending request even if files are null to let Backend handle validation
                         viewModel.completeProfile(
                             com.example.loanova_android.data.model.dto.UserProfileCompleteRequest(
                                 fullName = fullName,
                                 phoneNumber = phoneNumber,
                                 userAddress = address,
                                 nik = nik,
                                 birthDate = birthDate,
                                 npwpNumber = npwpNumber,
                                 ktpPhoto = ktpFile,
                                 profilePhoto = profileFile,
                                 npwpPhoto = npwpFile
                             )
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(8.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LoanovaBlue),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                         Text("Kirim Data", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Composable
fun LoanovaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    error: String? = null,
    keyboardType: androidx.compose.ui.text.input.KeyboardType = androidx.compose.ui.text.input.KeyboardType.Text,
    readOnly: Boolean = false
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            readOnly = readOnly,
            label = { Text(label) },
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (error != null) MaterialTheme.colorScheme.error else LoanovaBlue
                )
            },
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = LoanovaBlue,
                focusedLabelColor = LoanovaBlue,
                cursorColor = LoanovaBlue,
                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.7f),
                errorBorderColor = MaterialTheme.colorScheme.error,
                errorLabelColor = MaterialTheme.colorScheme.error
            ),
            isError = error != null,
            supportingText = {
                if (error != null) {
                    Text(
                        text = "# $error",
                        color = Color(0xFFEF4444), // Red color from Login Screen
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        )
    }
}

@Composable
fun FileUploadRow(label: String, uri: Uri?, error: String? = null, onClick: () -> Unit) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = if (error != null) Color(0xFFFEF2F2) else Color(0xFFF8FAFC)),
            elevation = CardDefaults.cardElevation(0.dp),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp, 
                color = if (error != null) MaterialTheme.colorScheme.error else Color.LightGray.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                     modifier = Modifier
                        .size(40.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(if (error != null) MaterialTheme.colorScheme.error.copy(alpha = 0.1f) else LoanovaBlue.copy(alpha = 0.1f)),
                     contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Upload, 
                        contentDescription = null, 
                        tint = if (error != null) MaterialTheme.colorScheme.error else LoanovaBlue
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = label, 
                        fontWeight = FontWeight.SemiBold, 
                        fontSize = 14.sp,
                        color = if (error != null) MaterialTheme.colorScheme.error else Color.Unspecified
                    )
                    Text(
                        text = if (uri != null) "File Terpilih" else "Belum ada file",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (uri != null) LoanovaBlue else Color.Gray,
                        fontWeight = if (uri != null) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
        if (error != null) {
            Text(
                text = "# $error",
                color = Color(0xFFEF4444),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

// Helper to convert Uri to File
fun uriToFile(context: Context, uri: Uri): File? {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val file = File.createTempFile("temp_image", ".jpg", context.cacheDir)
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        file
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
