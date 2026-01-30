package com.example.loanova_android.ui.features.profile.edit

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.loanova_android.core.common.ImageUtils
import com.example.loanova_android.ui.theme.LoanovaBlue
import com.example.loanova_android.ui.theme.LoanovaLightBlue
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Objects
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateUp: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // --- Camera & Gallery Logic (Using ImageUtils & Permission Check) ---
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var tempPhotoPath by remember { mutableStateOf<String?>(null) }
    var currentPhotoTarget by remember { mutableStateOf<String?>(null) } // "KTP", "PROFILE", "NPWP"
    var showSourceDialog by remember { mutableStateOf(false) }

    fun createImageUri(): Uri {
        val tempFile = File.createTempFile("camera_img_", ".jpg", context.cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }
        tempPhotoPath = tempFile.absolutePath
        return FileProvider.getUriForFile(
            Objects.requireNonNull(context),
            "${context.packageName}.provider",
            tempFile
        )
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoPath != null && currentPhotoTarget != null) {
            val rawFile = File(tempPhotoPath!!)
            // Process (Rotate & Compress) Immediately
            val processedFile = ImageUtils.processFile(rawFile)
             
             if (processedFile != null) {
                 when (currentPhotoTarget) {
                     "KTP" -> viewModel.onKtpPhotoSelected(processedFile)
                     "PROFILE" -> viewModel.onProfilePhotoSelected(processedFile)
                     "NPWP" -> viewModel.onNpwpPhotoSelected(processedFile)
                 }
             } else {
                 Toast.makeText(context, "Gagal memproses gambar", Toast.LENGTH_SHORT).show()
             }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                val uri = createImageUri()
                tempPhotoUri = uri
                cameraLauncher.launch(uri)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Gagal membuka kamera: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(context, "Izin kamera diperlukan untuk mengambil foto", Toast.LENGTH_SHORT).show()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null && currentPhotoTarget != null) {
            val file = ImageUtils.uriToFile(context, uri)
             if (file != null) {
                when (currentPhotoTarget) {
                    "KTP" -> viewModel.onKtpPhotoSelected(file)
                    "PROFILE" -> viewModel.onProfilePhotoSelected(file)
                    "NPWP" -> viewModel.onNpwpPhotoSelected(file)
                }
             } else {
                 Toast.makeText(context, "Gagal memproses gambar", Toast.LENGTH_SHORT).show()
             }
        }
    }

    fun openImagePicker(target: String) {
        currentPhotoTarget = target
        showSourceDialog = true
    }
    
    if (showSourceDialog) {
        ImageSourceOptionDialog(
            onDismiss = { showSourceDialog = false },
            onCameraClick = {
                showSourceDialog = false
                val permissionToCheck = android.Manifest.permission.CAMERA
                val isGranted = androidx.core.content.ContextCompat.checkSelfPermission(
                    context, 
                    permissionToCheck
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                
                if (isGranted) {
                    try {
                        val uri = createImageUri()
                        tempPhotoUri = uri
                        cameraLauncher.launch(uri)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(context, "Gagal membuka kamera: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                } else {
                    permissionLauncher.launch(permissionToCheck)
                }
            },
            onGalleryClick = {
                showSourceDialog = false
                galleryLauncher.launch("image/*")
            }
        )
    }
    // --- End Camera Logic ---

    LaunchedEffect(uiState.success) {
        if (uiState.success != null) {
            Toast.makeText(context, "Profil Berhasil Diupdate!", Toast.LENGTH_SHORT).show()
            onNavigateUp()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profil", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LoanovaBlue)
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = LoanovaBlue)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                 // Global Error Message (Styled Box)
                AnimatedVisibility(
                    visible = uiState.error != null && uiState.error?.contains("VALIDATION_ERROR") == false,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
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
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(20.dp).clickable { viewModel.onClearError() }
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

                // Form Fields
                EditProfileTextField(
                    value = uiState.fullName,
                    onValueChange = viewModel::onFullNameChange,
                    label = "Nama Lengkap",
                    icon = Icons.Outlined.Person,
                    errorMessage = uiState.fieldErrors?.get("fullName")
                )

                EditProfileTextField(
                    value = uiState.phoneNumber,
                    onValueChange = viewModel::onPhoneNumberChange,
                    label = "Nomor Telepon",
                    icon = Icons.Outlined.Phone,
                    errorMessage = uiState.fieldErrors?.get("phoneNumber"),
                    keyboardType = KeyboardType.Phone
                )

                EditProfileTextField(
                    value = uiState.userAddress,
                    onValueChange = viewModel::onUserAddressChange,
                    label = "Alamat Lengkap",
                    icon = Icons.Outlined.Home,
                    errorMessage = uiState.fieldErrors?.get("userAddress"),
                    singleLine = false
                )

                EditProfileTextField(
                    value = uiState.nik,
                    onValueChange = viewModel::onNikChange,
                    label = "NIK (16 Digit)",
                    icon = Icons.Outlined.CreditCard,
                    errorMessage = uiState.fieldErrors?.get("nik"),
                    keyboardType = KeyboardType.Number,
                    maxLength = 16
                )
                
                // Date Picker (Simplified Trigger)
                val dateDialogState = rememberDatePickerState()
                var showDateDialog by remember { mutableStateOf(false) }
                
                 if (showDateDialog) {
                    DatePickerDialog(
                        onDismissRequest = { showDateDialog = false },
                        confirmButton = {
                            TextButton(onClick = {
                                val selectedDate = dateDialogState.selectedDateMillis
                                if (selectedDate != null) {
                                    val date = java.time.Instant.ofEpochMilli(selectedDate)
                                        .atZone(java.time.ZoneId.systemDefault())
                                        .toLocalDate()
                                    viewModel.onBirthDateChange(date.toString())
                                }
                                showDateDialog = false
                            }) { Text("OK") }
                        },
                        dismissButton = { TextButton(onClick = { showDateDialog = false }) { Text("Cancel") } }
                    ) { DatePicker(state = dateDialogState) }
                }

                // Date Field (Using same style as text fields but clickable)
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    OutlinedTextField(
                        value = uiState.birthDate,
                        onValueChange = {},
                        label = { Text("Tanggal Lahir") },
                        leadingIcon = { 
                             Icon(
                                Icons.Outlined.DateRange, 
                                contentDescription = null,
                                tint = if (uiState.fieldErrors?.get("birthDate") != null) MaterialTheme.colorScheme.error else LoanovaBlue
                             ) 
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDateDialog = true },
                        enabled = false, // Disable typing, only click
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = Color.Black,
                            disabledBorderColor = if(uiState.fieldErrors?.get("birthDate") != null) MaterialTheme.colorScheme.error else  Color.LightGray.copy(alpha = 0.7f),
                            disabledLeadingIconColor = LoanovaBlue,
                            disabledLabelColor = if(uiState.fieldErrors?.get("birthDate") != null) MaterialTheme.colorScheme.error else Color.Gray,
                            errorBorderColor = MaterialTheme.colorScheme.error
                        ),
                        isError = uiState.fieldErrors?.get("birthDate") != null,
                        shape = RoundedCornerShape(16.dp)
                    )
                     // Field Error Text
                    if (uiState.fieldErrors?.get("birthDate") != null) {
                        Text(
                            text = "# ${uiState.fieldErrors?.get("birthDate")!!}",
                            color = Color(0xFFEF4444),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 4.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                EditProfileTextField(
                    value = uiState.npwpNumber,
                    onValueChange = viewModel::onNpwpNumberChange,
                    label = "Nomor NPWP (Opsional)",
                    icon = Icons.Outlined.Description,
                    errorMessage = uiState.fieldErrors?.get("npwpNumber"),
                    keyboardType = KeyboardType.Number,
                    maxLength = 16
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "Update Dokumen (Opsional)", 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Bold,
                    color = LoanovaBlue
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                // File Uploads
                FilePickerItem("Foto KTP", uiState.ktpPhoto, onClick = { openImagePicker("KTP") }, error = uiState.fieldErrors?.get("ktpPhoto"))
                FilePickerItem("Foto Profil", uiState.profilePhoto, onClick = { openImagePicker("PROFILE") }, error = uiState.fieldErrors?.get("profilePhoto"))
                FilePickerItem("Foto NPWP (Opsional)", uiState.npwpPhoto, onClick = { openImagePicker("NPWP") }, error = uiState.fieldErrors?.get("npwpPhoto"))

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.updateProfile() },
                    modifier = Modifier.fillMaxWidth().height(56.dp).shadow(8.dp, RoundedCornerShape(16.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = LoanovaBlue),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Simpan Perubahan", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

// Reusing style from CompleteProfileScreen (LoanovaTextField simplified)
@Composable
fun EditProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    errorMessage: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    maxLength: Int = Int.MAX_VALUE
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = { 
                if (it.length <= maxLength) {
                    // Filter numeric if needed
                    if (keyboardType == KeyboardType.Number || keyboardType == KeyboardType.Phone) {
                        if (it.all { char -> char.isDigit() }) onValueChange(it)
                    } else {
                        onValueChange(it)
                    }
                }
            },
            label = { Text(label) },
            leadingIcon = { 
                Icon(
                    imageVector = icon, 
                    contentDescription = null,
                    tint = if (errorMessage != null) MaterialTheme.colorScheme.error else LoanovaBlue
                ) 
            },
            isError = errorMessage != null,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = singleLine,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = LoanovaBlue,
                focusedLabelColor = LoanovaBlue,
                cursorColor = LoanovaBlue,
                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.7f),
                errorBorderColor = MaterialTheme.colorScheme.error,
                errorLabelColor = MaterialTheme.colorScheme.error
            )
        )
        if (errorMessage != null) {
            Text(
                text = "# $errorMessage",
                color = Color(0xFFEF4444),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun FilePickerItem(label: String, file: File?, onClick: () -> Unit, error: String? = null) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
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
                        Icons.Default.UploadFile, 
                        contentDescription = null, 
                        tint = if (error != null) MaterialTheme.colorScheme.error else LoanovaBlue
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = label, 
                        fontWeight = FontWeight.SemiBold, 
                        fontSize = 14.sp,
                        color = if (error != null) MaterialTheme.colorScheme.error else Color.Unspecified
                    )
                    if (file != null) {
                        Text("File terpilih", style = MaterialTheme.typography.bodySmall, color = LoanovaBlue, fontWeight = FontWeight.Bold)
                    } else {
                        Text("Klik untuk ganti foto", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
                if (file != null) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50))
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

@Composable
fun ImageSourceOptionDialog(
    onDismiss: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.padding(16.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Pilih Sumber Gambar",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = LoanovaBlue
                )
                Spacer(modifier = Modifier.height(24.dp))
                
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
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = LoanovaBlue.copy(alpha = 0.1f),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Kamera",
                                    tint = LoanovaBlue,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Kamera", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }

                    // Gallery Option
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onGalleryClick() }
                    ) {
                         Surface(
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = LoanovaLightBlue.copy(alpha = 0.1f),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = "Galeri",
                                    tint = LoanovaBlue,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Galeri", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                TextButton(onClick = onDismiss) {
                    Text("Batal", color = Color.Gray)
                }
            }
        }
    }
}
