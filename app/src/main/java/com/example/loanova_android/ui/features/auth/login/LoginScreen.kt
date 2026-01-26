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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
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
    viewModel: LoginViewModel = hiltViewModel(), // Hilt menyediakan ViewModel
    onNavigateToDashboard: (String) -> Unit      // Callback dari Navigation
) {
    // Collect state dari ViewModel sebagai Compose State
    // collectAsState() membuat UI re-compose ketika state berubah
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

    // Delegate UI rendering ke Dumb Composable
    // Passing state dan callbacks sebagai parameter
    LoginScreenContent(
        uiState = uiState,                    // Read-only state
        onLoginClick = viewModel::login,      // Method reference ke ViewModel
        onClearError = viewModel::clearError  // Method reference ke ViewModel
    )
}

@Composable
fun LoginScreenContent(
    uiState: LoginUiState,
    onLoginClick: (String, String) -> Unit,
    onClearError: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val isPreview = LocalInspectionMode.current

    val scale by if (isPreview) {
        remember { mutableStateOf(1f) }
    } else {
        val infiniteTransition = rememberInfiniteTransition(label = "logo_anim")
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ), label = "scale"
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LoanovaBackground)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_nova),
                    contentDescription = "Loanova Logo",
                    modifier = Modifier.size(110.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.login_title),
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = LoanovaBlue
            )
            Text(
                text = stringResource(R.string.login_subtitle),
                fontSize = 14.sp,
                color = Color.Gray.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(48.dp))

            if (uiState.error != null) {
                Text(
                    text = uiState.error,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp),
                    textAlign = TextAlign.Center
                )
            }

            OutlinedTextField(
                value = username,
                onValueChange = {
                    username = it
                    onClearError()
                },
                label = { Text(stringResource(R.string.username_label)) },
                placeholder = { Text(stringResource(R.string.username_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LoanovaBlue,
                    focusedLabelColor = LoanovaBlue,
                    cursorColor = LoanovaBlue
                ),
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    onClearError()
                },
                label = { Text(stringResource(R.string.password_label)) },
                placeholder = { Text(stringResource(R.string.password_placeholder)) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    val description = if (passwordVisible) stringResource(R.string.hide_password) else stringResource(R.string.show_password)
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = description)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LoanovaBlue,
                    focusedLabelColor = LoanovaBlue,
                    cursorColor = LoanovaBlue
                ),
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onLoginClick(username, password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LoanovaBlue),
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
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = stringResource(R.string.no_account), color = Color.Gray, fontSize = 14.sp)
                TextButton(onClick = { /* TODO: Register */ }) {
                    Text(text = stringResource(R.string.register_button), color = LoanovaBlue, fontWeight = FontWeight.Bold)
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
            onClearError = { }
        )
    }
}

@Preview(showBackground = true, name = "Loading State")
@Composable
fun LoginScreenLoadingPreview() {
    Loanova_androidTheme {
        LoginScreenContent(
            uiState = LoginUiState(isLoading = true),
            onLoginClick = { _, _ -> },
            onClearError = { }
        )
    }
}

@Preview(showBackground = true, name = "Error State")
@Composable
fun LoginScreenErrorPreview() {
    Loanova_androidTheme {
        LoginScreenContent(
            uiState = LoginUiState(error = "Invalid username or password"),
            onLoginClick = { _, _ -> },
            onClearError = { }
        )
    }
}
