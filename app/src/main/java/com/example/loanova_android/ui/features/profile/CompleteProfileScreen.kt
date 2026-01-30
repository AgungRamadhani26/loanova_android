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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.UploadFile
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.loanova_android.R
import com.example.loanova_android.ui.theme.LoanovaBlue
import com.example.loanova_android.ui.theme.LoanovaLightBlue
import com.example.loanova_android.ui.theme.LoanovaBackground
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Objects


/**
 * Layar Lengkapi Profil (Complete Profile Screen).
 * 
 * Screen ini digunakan oleh user baru untuk melengkapi data diri setelah registrasi.
 * Fitur utama:
 * 1. Form Data Diri (Nama, Telepon, Alamat, NIK, Tanggal Lahir, NPWP).
 * 2. Upload Dokumen (Foto Profil, KTP, NPWP) menggunakan Kamera atau Galeri.
 * 3. Kompresi Gambar Otomatis (Max 1MB) sebelum dikirim ke server.
 * 
 * @param onNavigateBack Callback untuk kembali ke layar sebelumnya.
 * @param onSuccess Callback ketika profil berhasil disimpan (misal: navigasi ke Home).
 * @param viewModel ViewModel untuk mengatur logic dan state dari screen ini.
 */
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

    // --- CAMERA & GALLERY LOGIC ---
    // Dialog state: Apakah popup "Pilih Kamera/Galeri" sedang muncul?
    var showImageSourceDialog by remember { mutableStateOf(false) }
    
    // Target state: Foto mana yang sedang diedit? ("ktp", "profile", atau "npwp")
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
        val tempFile = File.createTempFile("camera_img_", ".jpg", context.cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }
        tempCameraPath = tempFile.absolutePath
        return FileProvider.getUriForFile(
            Objects.requireNonNull(context),
            "com.example.loanova_android.provider", // Hardcoded to match Manifest
            tempFile
        )
    }

    // Launchers
    val galleryLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? -> 
        uri?.let { 
            when(currentImageTarget) {
                "ktp" -> ktpUri = it
                "profile" -> profileUri = it
                "npwp" -> npwpUri = it
            }
            viewModel.clearError() 
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicture()) { success ->
        if (success && tempCameraPath != null) {
            val rawFile = File(tempCameraPath!!)
            
            // Process (Rotate & Compress) Immediately using Path
            val processedFile = com.example.loanova_android.core.common.ImageUtils.processFile(rawFile)
            
            if (processedFile != null) {
                val newUri = Uri.fromFile(processedFile)
                when(currentImageTarget) {
                    "ktp" -> ktpUri = newUri
                    "profile" -> profileUri = newUri
                    "npwp" -> npwpUri = newUri
                }
                viewModel.clearError()
            } else {
               Toast.makeText(context, "Gagal memproses gambar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Permission Launcher: Menangani permintaan izin kamera secara Runtime (Wajib untuk Android 6.0+)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Jika user mengizinkan, langsung buka kamera
            try {
                val uri = createTempPictureUri()
                tempCameraUri = uri
                cameraLauncher.launch(uri)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Gagal membuka kamera: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } else {
            // Jika user menolak, tampilkan pesan edukasi
            Toast.makeText(context, "Izin kamera diperlukan untuk mengambil foto", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to handle click
    fun openImageSelection(target: String) {
        currentImageTarget = target
        showImageSourceDialog = true
    }

    if (showImageSourceDialog) {
        ImageSourceOptionDialog(
            onDismiss = { showImageSourceDialog = false },
            onCameraClick = {
                showImageSourceDialog = false
                val permissionToCheck = android.Manifest.permission.CAMERA
                val isGranted = androidx.core.content.ContextCompat.checkSelfPermission(
                    context, 
                    permissionToCheck
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                
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
                    permissionLauncher.launch(permissionToCheck)
                }
            },
            onGalleryClick = {
                showImageSourceDialog = false
                galleryLauncher.launch("image/*")
            }
        )
    }

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
                    keyboardType = KeyboardType.Number
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
                    keyboardType = KeyboardType.Number
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
                                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                        birthDate = sdf.format(Date(millis))
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
                    onClick = { openImageSelection("profile") }
                )
                FileUploadRow(
                    label = "Foto KTP (Wajib)", 
                    uri = ktpUri, 
                    error = uiState.fieldErrors?.get("ktpPhoto"),
                    onClick = { openImageSelection("ktp") }
                )
                FileUploadRow(
                    label = "Foto NPWP (Opsional)", 
                    uri = npwpUri, 
                    error = uiState.fieldErrors?.get("npwpPhoto"),
                    onClick = { openImageSelection("npwp") }
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
    keyboardType: KeyboardType = KeyboardType.Text,
    readOnly: Boolean = false
) {
    Column(modifier = Modifier.padding(vertical = 0.dp)) {
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
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
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
                    imageVector = Icons.Outlined.UploadFile, 
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
                if (uri != null) {
                    Text("File Terpilih", style = MaterialTheme.typography.bodySmall, color = LoanovaBlue, fontWeight = FontWeight.Bold)
                } else {
                    Text("Klik untuk upload foto", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
            if (uri != null) {
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

/**
 * Mengubah URI (dari Galeri/Kamera) menjadi File fisik.
 * Fitur Spesial: Mengompresi gambar jika ukurannya > 1MB.
 * 
 * Algoritma:
 * 1. Baca Stream dari URI.
 * 2. Decode menjadi Bitmap (gambar di memori).
 * 3. Cek ukuran stream. Jika > 1MB, turunkan kualitas (Quality) sebesar 5%.
 * 4. Ulangi sampai ukuran <= 1MB atau kualitas < 5%.
 * 5. Simpan hasil akhir ke File sementara (.jpg).
 */
fun uriToFile(context: Context, uri: Uri): File? {
    return try {
        val contentResolver = context.contentResolver
        val file = File.createTempFile("temp_image", ".jpg", context.cacheDir)

        // 1. Decode stream to Bitmap
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        if (bitmap == null) return null

        // 2. Compress Bitmap Logic
        var compressQuality = 100
        var streamLength: Int
        
        // Loop: Tekan terus sampai ukurannya pas (< 1MB)
        do {
            val bmpStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
            val bmpPicByteArray = bmpStream.toByteArray()
            streamLength = bmpPicByteArray.size
            
            if (streamLength > 1_000_000) {
                compressQuality -= 5 // Kurangi kualitas 5% setiap iterasi
            }
            
            // Safety break: Jangan sampai kualitas 0 atau loop infinite
            if (compressQuality < 5) break
            
            bmpStream.close()
        } while (streamLength > 1_000_000)

        // 3. Save Compressed Bitmap ke File Fisik
        val fos = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, fos)
        fos.flush()
        fos.close()
        
        file
    } catch (e: Exception) {
        e.printStackTrace()
        null
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
