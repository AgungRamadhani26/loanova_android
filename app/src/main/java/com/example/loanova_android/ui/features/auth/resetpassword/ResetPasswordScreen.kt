package com.example.loanova_android.ui.features.auth.resetpassword

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

// Color palette for reset password screen
private val ResetPrimaryColor = Color(0xFF0EA5E9)       // Sky Blue
private val ResetSecondaryColor = Color(0xFF38BDF8)     // Light Sky Blue
private val ResetGradientStart = Color(0xFF0C4A6E)      // Dark Sky Blue
private val ResetGradientEnd = Color(0xFF0284C7)        // Medium Sky Blue
private val ResetAccentColor = Color(0xFF7DD3FC)        // Bright Sky Blue

/**
 * ResetPasswordScreen - Halaman untuk reset password dengan token dari deep link.
 * 
 * FLOW:
 * 1. User klik link dari email (deep link)
 * 2. App extract token dari deep link dan navigasi ke screen ini
 * 3. User memasukkan password baru
 * 4. Backend validasi token dan update password
 * 5. Redirect ke Login
 * 
 * @param onNavigateToLogin Callback untuk navigasi ke Login setelah sukses
 * @param viewModel ViewModel untuk Reset Password
 */
@Composable
fun ResetPasswordScreen(
    onNavigateToLogin: () -> Unit,
    viewModel: ResetPasswordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Success Dialog
    if (uiState.passwordReset) {
        AlertDialog(
            onDismissRequest = { /* Prevent dismiss */ },
            confirmButton = {
                TextButton(onClick = onNavigateToLogin) {
                    Text("Login Sekarang", color = ResetPrimaryColor, fontWeight = FontWeight.Bold)
                }
            },
            title = { 
                Text(
                    "Password Berhasil Diubah!", 
                    fontWeight = FontWeight.Bold,
                    color = ResetPrimaryColor
                ) 
            },
            text = { 
                Text("Kata sandi Anda telah berhasil diubah. Silakan login dengan password baru.") 
            },
            icon = { 
                Icon(
                    Icons.Default.CheckCircle, 
                    contentDescription = null, 
                    tint = ResetPrimaryColor,
                    modifier = Modifier.size(48.dp)
                ) 
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

    ResetPasswordScreenContent(
        uiState = uiState,
        onSubmit = viewModel::resetPassword,
        onClearError = viewModel::clearError,
        onTokenChange = viewModel::setToken
    )
}

@Composable
fun ResetPasswordScreenContent(
    uiState: ResetPasswordUiState,
    onSubmit: (String, String) -> Unit,
    onClearError: () -> Unit,
    onTokenChange: (String) -> Unit
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var showTokenInput by remember { mutableStateOf(uiState.token.isBlank()) }

    // Update showTokenInput when token changes
    LaunchedEffect(uiState.token) {
        showTokenInput = uiState.token.isBlank()
    }

    // Background Container
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F9FF)) // Light sky blue background
    ) {
        // Upper Section with Modern Gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            ResetGradientStart,
                            ResetPrimaryColor,
                            ResetGradientEnd
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
            
            // Header Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 50.dp),
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
                        imageVector = Icons.Default.VpnKey,
                        contentDescription = "Reset Password",
                        tint = ResetPrimaryColor,
                        modifier = Modifier.size(50.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Reset Kata Sandi",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Buat kata sandi baru untuk akun Anda",
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
                
                // Token Input (Manual Fallback)
                AnimatedVisibility(visible = showTokenInput) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Masukkan token dari email:",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        OutlinedTextField(
                            value = uiState.token,
                            onValueChange = { 
                                onTokenChange(it)
                                onClearError()
                            },
                            label = { Text("Token") },
                            placeholder = { Text("Paste token dari email") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = null,
                                    tint = ResetPrimaryColor
                                )
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ResetPrimaryColor,
                                focusedLabelColor = ResetPrimaryColor,
                                cursorColor = ResetPrimaryColor
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                
                // Token Status Badge (when token from deep link)
                if (!showTokenInput && uiState.token.isNotBlank()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFDCFCE7)
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
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Token terdeteksi dari link",
                                color = Color(0xFF16A34A),
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(
                                onClick = { showTokenInput = true },
                                contentPadding = PaddingValues(4.dp)
                            ) {
                                Text(
                                    text = "Ubah",
                                    color = Color(0xFF16A34A),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
                
                // Password Requirements Info
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF0F9FF)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = ResetPrimaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Password harus minimal 8 karakter, mengandung huruf besar, huruf kecil, dan angka.",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
                
                // New Password Field
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { 
                        newPassword = it
                        onClearError()
                    },
                    label = { Text("Password Baru") },
                    placeholder = { Text("Masukkan password baru") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = ResetPrimaryColor
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                tint = Color.Gray
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ResetPrimaryColor,
                        focusedLabelColor = ResetPrimaryColor,
                        cursorColor = ResetPrimaryColor
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Confirm Password Field
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { 
                        confirmPassword = it
                        onClearError()
                    },
                    label = { Text("Konfirmasi Password") },
                    placeholder = { Text("Ulangi password baru") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = ResetPrimaryColor
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password",
                                tint = Color.Gray
                            )
                        }
                    },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ResetPrimaryColor,
                        focusedLabelColor = ResetPrimaryColor,
                        cursorColor = ResetPrimaryColor
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Submit Button
                Button(
                    onClick = { onSubmit(newPassword, confirmPassword) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !uiState.isLoading,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ResetPrimaryColor,
                        disabledContainerColor = ResetPrimaryColor.copy(alpha = 0.5f)
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
                            imageVector = Icons.Default.LockReset,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Reset Password",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
