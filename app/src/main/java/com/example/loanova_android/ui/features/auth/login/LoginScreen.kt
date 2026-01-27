package com.example.loanova_android.ui.features.auth.login

// ============================================================================
// LAYER: UI (Presentation Layer)
// PATTERN: Jetpack Compose with MVVM
// RESPONSIBILITY: Login screen UI dengan Smart/Dumb composable pattern
// ============================================================================

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.loanova_android.R
import com.example.loanova_android.ui.theme.LoanovaBlue
import com.example.loanova_android.ui.theme.LoanovaLightBlue
import com.example.loanova_android.ui.theme.LoanovaBackground
import com.example.loanova_android.ui.theme.Loanova_androidTheme

// ============================================================================
// SMART COMPOSABLE (Container/Screen)
// - Memiliki akses ke ViewModel
// - Handle side-effects (navigation, etc.)
// - Mendelegasikan rendering ke Dumb Composable
// ============================================================================

/**
 * LoginScreen - Smart Composable untuk halaman Login.
 * 
 * APA ITU SMART COMPOSABLE?
 * - Composable yang "aware" terhadap business logic dan state management
 * - Memiliki akses ke ViewModel
 * - Handle side-effects (navigation, analytics, etc.)
 * - Tidak/minimal memiliki UI code langsung
 * 
 * PATTERN: SCREEN COMPOSABLE
 * - Entry point untuk sebuah feature/screen
 * - Biasanya menerima ViewModel dan navigation callbacks
 * - Mendelegasikan UI rendering ke Content composable
 * 
 * @param viewModel ViewModel untuk login logic, di-inject oleh Hilt
 * @param onNavigateToDashboard Callback untuk navigasi ke Dashboard setelah login sukses
 */
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onNavigateToDashboard: (String) -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // ========================================================================
    // SIDE-EFFECT: Navigation setelah login sukses
    // ========================================================================
    // LaunchedEffect: Menjalankan side-effect ketika key berubah
    // Key = uiState.success: Effect dijalankan ketika success berubah
    LaunchedEffect(uiState.success) {
        uiState.success?.let {
            // Jika success tidak null, navigasi ke Dashboard
            // it.username: mengambil username dari User object
            onNavigateToDashboard(it.username)
        }
    }

    LoginScreenContent(
        uiState = uiState,
        onLoginClick = viewModel::login,
        onClearError = viewModel::clearError,
        onRegisterClick = onNavigateToRegister
    )
}

@Composable
fun LoginScreenContent(
    uiState: LoginUiState,
    onLoginClick: (String, String) -> Unit,
    onClearError: () -> Unit,
    onRegisterClick: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Background Container
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LoanovaBackground)
    ) {
        // Upper Blue Section (Header)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(LoanovaBlue, LoanovaLightBlue)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo Container
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color.White.copy(alpha = 0.2f), shape = androidx.compose.foundation.shape.CircleShape)
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
                    text = "Selamat Datang",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = "Masuk untuk melanjutkan",
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
                .padding(top = 260.dp), // Overlap with header
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                
                if (uiState.error != null) {
                    Surface(
                        color = Color(0xFFFDE8E8), // Pink background
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center // Matches the centered look in user's screenshot, but web might be left. 
                            // User's screenshot had it entered. Web screenshot had it left? 
                            // Web screenshot 1: "Validasi gagal" is Left aligned next to icon.
                            // Web screenshot 2: "Username atau password salah" is Left aligned.
                            // Android screenshot: Centered.
                            // User said "kamu buta ya", implying strong mismatch.
                            // I will change it to Start (Left) alignment to match Web exactly.
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = Color(0xFFEF4444), // Red color
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = uiState.error,
                                color = Color(0xFFEF4444),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Start // Left aligned like Web
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
                    label = { Text(stringResource(R.string.username_label)) },
                    placeholder = { Text(stringResource(R.string.username_placeholder)) },
                    leadingIcon = {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Person,
                            contentDescription = null,
                            tint = if (usernameError != null) MaterialTheme.colorScheme.error else LoanovaBlue
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LoanovaBlue,
                        focusedLabelColor = LoanovaBlue,
                        cursorColor = LoanovaBlue,
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

                // Password Input
                val passwordError = uiState.fieldErrors?.get("password")
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        onClearError()
                    },
                    label = { Text(stringResource(R.string.password_label)) },
                    placeholder = { Text(stringResource(R.string.password_placeholder)) },
                    leadingIcon = {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (passwordError != null) MaterialTheme.colorScheme.error else LoanovaBlue
                        )
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        val description = if (passwordVisible) stringResource(R.string.hide_password) else stringResource(R.string.show_password)
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = description, tint = Color.Gray)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LoanovaBlue,
                        focusedLabelColor = LoanovaBlue,
                        cursorColor = LoanovaBlue,
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
                        }
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Login Button
                Button(
                    onClick = { onLoginClick(username, password) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(8.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LoanovaBlue,
                        disabledContainerColor = LoanovaBlue.copy(alpha = 0.5f)
                    ),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.login_button),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Footer
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.no_account),
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    TextButton(onClick = onRegisterClick) {
                        Text(
                            text = stringResource(R.string.register_button),
                            color = LoanovaBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Default State")
@Composable
fun LoginScreenPreview() {
    Loanova_androidTheme {
        LoginScreenContent(
            uiState = LoginUiState(),
            onLoginClick = { _, _ -> },
            onClearError = { },
            onRegisterClick = {}
        )
    }
}
