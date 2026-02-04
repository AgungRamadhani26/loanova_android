package com.example.loanova_android.ui.features.auth.forgotpassword

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.loanova_android.R

// Color palette for forgot password screen
private val ForgotPrimaryColor = Color(0xFF7C3AED)      // Purple
private val ForgotSecondaryColor = Color(0xFF8B5CF6)    // Light Purple
private val ForgotGradientStart = Color(0xFF4C1D95)     // Dark Purple
private val ForgotGradientEnd = Color(0xFF6D28D9)       // Medium Purple
private val ForgotAccentColor = Color(0xFFA78BFA)       // Bright Purple

/**
 * ForgotPasswordScreen - Halaman untuk request reset password.
 * 
 * FLOW:
 * 1. User memasukkan email
 * 2. Klik tombol "Kirim Link Reset"
 * 3. Backend kirim email dengan link reset password
 * 4. UI menampilkan pesan sukses dan instruksi cek email
 * 
 * @param onNavigateBack Callback untuk kembali ke halaman sebelumnya
 * @param onNavigateToLogin Callback untuk navigasi ke Login
 * @param viewModel ViewModel untuk Forgot Password
 */
@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Success Dialog
    if (uiState.emailSent) {
        AlertDialog(
            onDismissRequest = { /* Prevent dismiss */ },
            confirmButton = {
                TextButton(onClick = onNavigateToLogin) {
                    Text("Ke Halaman Login", color = ForgotPrimaryColor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.resetState() }) {
                    Text("Kirim Ulang", color = Color.Gray)
                }
            },
            title = { 
                Text(
                    "Email Terkirim!", 
                    fontWeight = FontWeight.Bold,
                    color = ForgotPrimaryColor
                ) 
            },
            text = { 
                Text(
                    "Link reset password telah dikirim ke email Anda. " +
                    "Silakan cek inbox atau folder spam. Link akan kadaluarsa dalam 5 menit."
                ) 
            },
            icon = { 
                Icon(
                    Icons.Default.MarkEmailRead, 
                    contentDescription = null, 
                    tint = ForgotPrimaryColor,
                    modifier = Modifier.size(48.dp)
                ) 
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

    ForgotPasswordScreenContent(
        uiState = uiState,
        onSubmit = viewModel::forgotPassword,
        onClearError = viewModel::clearError,
        onNavigateBack = onNavigateBack
    )
}

@Composable
fun ForgotPasswordScreenContent(
    uiState: ForgotPasswordUiState,
    onSubmit: (String) -> Unit,
    onClearError: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }

    // Background Container
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAF5FF)) // Light purple background
    ) {
        // Upper Section with Modern Gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            ForgotGradientStart,
                            ForgotPrimaryColor,
                            ForgotGradientEnd
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
                    color = Color.White.copy(alpha = 0.03f),
                    radius = size.width * 0.25f,
                    center = Offset(size.width * 0.1f, size.height * 0.7f)
                )
            }
            
            // Back button
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopStart)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Kembali",
                    tint = Color.White
                )
            }
            
            // Header Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 70.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon Container
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .shadow(
                            elevation = 20.dp,
                            shape = CircleShape,
                            spotColor = Color.Black.copy(alpha = 0.3f)
                        )
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LockReset,
                        contentDescription = "Forgot Password",
                        tint = ForgotPrimaryColor,
                        modifier = Modifier.size(50.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Lupa Kata Sandi?",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Masukkan email untuk reset password",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }
        }
        
        // Form Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 240.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Error Message
                AnimatedVisibility(
                    visible = uiState.error != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFEE2E2)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = uiState.error ?: "",
                                color = Color(0xFFDC2626),
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = onClearError,
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
                
                // Instruction Text
                Text(
                    text = "Kami akan mengirimkan link untuk mereset kata sandi ke email yang terdaftar.",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                // Email Field
                OutlinedTextField(
                    value = email,
                    onValueChange = { 
                        email = it
                        onClearError()
                    },
                    label = { Text("Email") },
                    placeholder = { Text("Masukkan email Anda") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = ForgotPrimaryColor
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ForgotPrimaryColor,
                        focusedLabelColor = ForgotPrimaryColor,
                        cursorColor = ForgotPrimaryColor
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Submit Button
                Button(
                    onClick = { onSubmit(email) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !uiState.isLoading,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ForgotPrimaryColor,
                        disabledContainerColor = ForgotPrimaryColor.copy(alpha = 0.5f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 2.dp
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Kirim Link Reset",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Back to Login Link
                TextButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = ForgotPrimaryColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Kembali ke Login",
                        color = ForgotPrimaryColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
