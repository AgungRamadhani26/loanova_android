package com.example.loanova_android.ui.features.auth.register

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.loanova_android.R
import com.example.loanova_android.data.model.dto.RegisterRequest
import com.example.loanova_android.ui.theme.LoanovaBackground
import com.example.loanova_android.ui.theme.LoanovaBlue
import com.example.loanova_android.ui.theme.LoanovaLightBlue

// Modern color palette for register screen
private val RegisterPrimaryColor = Color(0xFF059669)    // Emerald Green
private val RegisterSecondaryColor = Color(0xFF10B981)  // Light Emerald
private val RegisterGradientStart = Color(0xFF064E3B)   // Dark Emerald
private val RegisterGradientEnd = Color(0xFF047857)     // Medium Emerald
private val RegisterAccentColor = Color(0xFF34D399)     // Bright Emerald

/**
 * Halaman Registrasi (Daftar Akun Baru).
 * 
 * Screen ini memfasilitasi user baru untuk membuat akun.
 * Fitur:
 * 1. Form Input (Username, Email, Password).
 * 2. Visual feedback untuk error validasi (field error) atau global error.
 * 3. Navigasi ke Login Screen jika sudah punya akun atau sukses daftar.
 * 4. Menampilkan Dialog Sukses setelah pendaftaran berhasil.
 * 
 * @param onNavigateToLogin Callback navigasi ke halaman Login.
 * @param viewModel ViewModel registrasi (di-inject Hilt).
 */
@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Alert Dialog for Success
    if (uiState.success) {
        AlertDialog(
            onDismissRequest = { /* Prevent dismiss without action */ },
            confirmButton = {
                TextButton(onClick = { 
                    viewModel.resetState()
                    onNavigateToLogin() 
                }) {
                    Text("Login Sekarang", color = RegisterPrimaryColor, fontWeight = FontWeight.Bold)
                }
            },
            title = { 
                Text(
                    "Pendaftaran Berhasil", 
                    fontWeight = FontWeight.Bold,
                    color = RegisterPrimaryColor
                ) 
            },
            text = { Text("Akun Anda telah berhasil dibuat. Silakan login untuk melanjutkan.") },
            icon = { 
                Icon(
                    Icons.Default.CheckCircle, 
                    contentDescription = null, 
                    tint = RegisterPrimaryColor,
                    modifier = Modifier.size(48.dp)
                ) 
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

    RegisterScreenContent(
        uiState = uiState,
        onRegister = viewModel::register,
        onClearError = viewModel::clearError,
        onNavigateToLogin = onNavigateToLogin
    )
}

@Composable
fun RegisterScreenContent(
    uiState: RegisterUiState,
    onRegister: (RegisterRequest) -> Unit,
    onClearError: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Background Container
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0FDF4)) // Light emerald background
    ) {
        // Upper Section with Modern Gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            RegisterGradientStart,
                            RegisterPrimaryColor,
                            RegisterGradientEnd
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
        ) {
            // Decorative circles
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.05f),
                    radius = size.width * 0.35f,
                    center = Offset(size.width * 0.85f, size.height * 0.25f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.08f),
                    radius = size.width * 0.25f,
                    center = Offset(size.width * 0.15f, size.height * 0.75f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.03f),
                    radius = size.width * 0.2f,
                    center = Offset(size.width * 0.5f, size.height * 0.1f)
                )
            }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo Container with glow effect
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.25f),
                                    Color.White.copy(alpha = 0.1f)
                                )
                            )
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_nova),
                        contentDescription = "Loanova Logo",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Daftar Akun Baru",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Lengkapi data diri untuk memulai",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color.White.copy(alpha = 0.8f)
                    )
                )
            }
        }

        // Bottom White Section (Form)
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 250.dp),
            shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                
                // Error Banner
                AnimatedVisibility(
                    visible = uiState.error != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Surface(
                        color = Color(0xFFFDE8E8), // Pink background
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.2f)),
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
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = uiState.error ?: "",
                                color = Color(0xFFEF4444),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Start
                            )
                        }
                    }
                }

                // Username Input
                val usernameError = uiState.fieldErrors?.get("username")
                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it
                        onClearError()
                    },
                    label = { Text("Username") },
                    placeholder = { Text("Masukkan username") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = if (usernameError != null) MaterialTheme.colorScheme.error else RegisterPrimaryColor
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RegisterPrimaryColor,
                        focusedLabelColor = RegisterPrimaryColor,
                        cursorColor = RegisterPrimaryColor,
                        unfocusedBorderColor = Color.LightGray,
                        errorBorderColor = MaterialTheme.colorScheme.error,
                        errorLabelColor = MaterialTheme.colorScheme.error
                    ),
                    enabled = !uiState.isLoading,
                    isError = usernameError != null,
                    supportingText = {
                        if (usernameError != null) {
                            Text(
                                text = "# $usernameError", 
                                color = Color(0xFFEF4444),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Email Input
                val emailError = uiState.fieldErrors?.get("email")
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        onClearError()
                    },
                    label = { Text("Email") },
                    placeholder = { Text("Masukkan email") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = if (emailError != null) MaterialTheme.colorScheme.error else RegisterPrimaryColor
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RegisterPrimaryColor,
                        focusedLabelColor = RegisterPrimaryColor,
                        cursorColor = RegisterPrimaryColor,
                        unfocusedBorderColor = Color.LightGray,
                        errorBorderColor = MaterialTheme.colorScheme.error,
                        errorLabelColor = MaterialTheme.colorScheme.error
                    ),
                    enabled = !uiState.isLoading,
                    isError = emailError != null,
                    supportingText = {
                        if (emailError != null) {
                            Text(
                                text = "# $emailError", 
                                color = Color(0xFFEF4444),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Password Input
                val passwordError = uiState.fieldErrors?.get("password")
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        onClearError()
                    },
                    label = { Text("Password") },
                    placeholder = { Text("Masukkan password") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (passwordError != null) MaterialTheme.colorScheme.error else RegisterPrimaryColor
                        )
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = null, tint = Color.Gray)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RegisterPrimaryColor,
                        focusedLabelColor = RegisterPrimaryColor,
                        cursorColor = RegisterPrimaryColor,
                        unfocusedBorderColor = Color.LightGray,
                        errorBorderColor = MaterialTheme.colorScheme.error,
                        errorLabelColor = MaterialTheme.colorScheme.error
                    ),
                    enabled = !uiState.isLoading,
                    isError = passwordError != null,
                    supportingText = {
                        if (passwordError != null) {
                            Text(
                                text = "# $passwordError", 
                                color = Color(0xFFEF4444),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        } else {
                             // Static helper text if no error
                             Text(
                                text = "Minimal 8 karakter, huruf besar, kecil & angka",
                                color = Color.Gray,
                                fontSize = 11.sp
                             )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Register Button
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 8.dp,
                    color = if (uiState.isLoading) RegisterPrimaryColor.copy(alpha = 0.5f) else RegisterPrimaryColor,
                    onClick = { 
                        if (!uiState.isLoading) {
                            onRegister(RegisterRequest(username, email, password)) 
                        }
                    }
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Daftar Sekarang",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Footer
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sudah punya akun?",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    TextButton(onClick = onNavigateToLogin) {
                        Text(
                            text = "Masuk",
                            color = RegisterPrimaryColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
