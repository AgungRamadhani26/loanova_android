package com.example.loanova_android.ui.features.profile.edit

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.loanova_android.ui.theme.LoanovaBlue
import java.io.File
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.example.loanova_android.R
import android.content.Context
import androidx.core.content.FileProvider
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.loanova_android.core.common.ImageUtils
import com.example.loanova_android.ui.features.profile.CompleteProfileUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateUp: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // --- Camera & Gallery Logic (Copied from CompleteProfileScreen) ---
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var currentPhotoTarget by remember { mutableStateOf<String?>(null) } // "KTP", "PROFILE", "NPWP"
    var showSourceDialog by remember { mutableStateOf(false) }

    fun createImageUri(): Uri {
        val file = File(context.cacheDir, "temp_camera_img_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoUri != null && currentPhotoTarget != null) {
             val file = ImageUtils.uriToFile(context, tempPhotoUri!!)
             val compressed = ImageUtils.reduceFileImage(file)
             when (currentPhotoTarget) {
                 "KTP" -> viewModel.onKtpPhotoSelected(compressed)
                 "PROFILE" -> viewModel.onProfilePhotoSelected(compressed)
                 "NPWP" -> viewModel.onNpwpPhotoSelected(compressed)
             }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null && currentPhotoTarget != null) {
            val file = ImageUtils.uriToFile(context, uri)
            val compressed = ImageUtils.reduceFileImage(file)
            when (currentPhotoTarget) {
                "KTP" -> viewModel.onKtpPhotoSelected(compressed)
                "PROFILE" -> viewModel.onProfilePhotoSelected(compressed)
                "NPWP" -> viewModel.onNpwpPhotoSelected(compressed)
            }
        }
    }

    fun openImagePicker(target: String) {
        currentPhotoTarget = target
        showSourceDialog = true
    }
    
    if (showSourceDialog) {
        AlertDialog(
            onDismissRequest = { showSourceDialog = false },
            title = { Text("Pilih Sumber Foto") },
            text = { Text("Ambil foto dari kamera atau galeri?") },
            confirmButton = {
                TextButton(onClick = {
                    showSourceDialog = false
                    tempPhotoUri = createImageUri()
                    cameraLauncher.launch(tempPhotoUri!!)
                }) { Text("Kamera") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showSourceDialog = false
                    galleryLauncher.launch("image/*")
                }) { Text("Galeri") }
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

    LaunchedEffect(uiState.error) {
         if (uiState.error != null && !uiState.error!!.contains("VALIDATION_ERROR")) {
             // Show Toast for general errors, validation errors shown in UI text
             Toast.makeText(context, uiState.error, Toast.LENGTH_LONG).show()
             viewModel.onClearError()
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

                OutlinedTextField(
                    value = uiState.birthDate,
                    onValueChange = {},
                    label = { Text("Tanggal Lahir") },
                    leadingIcon = { Icon(Icons.Outlined.DateRange, null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clickable { showDateDialog = true },
                    enabled = false, // Disable typing, only click
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color.Black,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    isError = uiState.fieldErrors?.get("birthDate") != null
                )
                 // Field Error Text
                if (uiState.fieldErrors?.get("birthDate") != null) {
                    Text(
                        text = uiState.fieldErrors?.get("birthDate")!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, bottom = 8.dp)
                    )
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
                
                Text("Update Dokumen (Opsional)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                
                // File Uploads
                FilePickerItem("Foto KTP", uiState.ktpPhoto, onClick = { openImagePicker("KTP") }, error = uiState.fieldErrors?.get("ktpPhoto"))
                FilePickerItem("Foto Profil", uiState.profilePhoto, onClick = { openImagePicker("PROFILE") }, error = uiState.fieldErrors?.get("profilePhoto"))
                FilePickerItem("Foto NPWP (Opsional)", uiState.npwpPhoto, onClick = { openImagePicker("NPWP") }, error = uiState.fieldErrors?.get("npwpPhoto"))

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.updateProfile() },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LoanovaBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Simpan Perubahan", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

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
    Column {
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
            leadingIcon = { Icon(icon, contentDescription = null) },
            isError = errorMessage != null,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = singleLine,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            shape = RoundedCornerShape(12.dp)
        )
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun FilePickerItem(label: String, file: File?, onClick: () -> Unit, error: String? = null) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        border = if (error != null) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.UploadFile, contentDescription = null, tint = LoanovaBlue)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, fontWeight = FontWeight.Medium)
                if (file != null) {
                    Text("File terpilih: ${file.name}", style = MaterialTheme.typography.bodySmall, color = LoanovaBlue)
                } else {
                    Text("Klik untuk ganti foto", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                
                if (error != null) {
                    Text(error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }
            if (file != null) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50))
            }
        }
    }
}
