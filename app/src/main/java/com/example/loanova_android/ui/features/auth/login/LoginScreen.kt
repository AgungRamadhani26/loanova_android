package com.example.loanova_android.ui.features.auth.login

// ============================================================================
// LAYER: UI (Presentation Layer)
// PATTERN: Jetpack Compose with MVVM
// RESPONSIBILITY: Login screen UI dengan Smart/Dumb composable pattern
// ============================================================================

import android.app.Activity
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.loanova_android.R
import com.example.loanova_android.ui.theme.LoanovaBlue
import com.example.loanova_android.ui.theme.LoanovaLightBlue
import com.example.loanova_android.ui.theme.LoanovaBackground
import com.example.loanova_android.ui.theme.Loanova_androidTheme
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Modern color palette for auth screens
private val AuthPrimaryColor = Color(0xFF1E3A5F)      // Deep Navy Blue
private val AuthSecondaryColor = Color(0xFF3B82F6)    // Bright Blue
private val AuthAccentColor = Color(0xFF10B981)       // Emerald Green
private val AuthGradientStart = Color(0xFF0F172A)     // Very Dark Blue
private val AuthGradientEnd = Color(0xFF1E40AF)       // Royal Blue

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
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // State for Google Sign-In loading
    var isGoogleLoading by remember { mutableStateOf(false) }
    var googleError by remember { mutableStateOf<String?>(null) }

    // ========================================================================
    // SIDE-EFFECT: Navigation setelah login sukses
    // ========================================================================
    // LaunchedEffect: Menjalankan side-effect ketika key berubah
    // Key = uiState.success: Effect dijalankan ketika success berubah
    LaunchedEffect(uiState.success) {
        uiState.success?.let {
            // Jika success tidak null, navigasi ke Home
            onLoginSuccess()
        }
    }
    
    // Google Sign-In handler
    val onGoogleSignInClick: () -> Unit = {
        coroutineScope.launch {
            isGoogleLoading = true
            googleError = null
            
            try {
                // Build Credential Manager request
                val credentialManager = CredentialManager.create(context)
                
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setServerClientId("42854164323-oa1i5pc9t5gnel1nqr7g5i50inntnqn5.apps.googleusercontent.com")
                    .setFilterByAuthorizedAccounts(false) // Allow user to select any account
                    .setAutoSelectEnabled(false) // Don't auto-select, let user choose
                    .build()
                
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()
                
                // Show Google Sign-In bottom sheet
                val result = credentialManager.getCredential(
                    context = context as Activity,
                    request = request
                )
                
                // Extract Google ID Token credential
                val credential = result.credential
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val googleIdToken = googleIdTokenCredential.idToken
                
                android.util.Log.d("LoginScreen", "Google ID Token received")
                
                // Sign in to Firebase with Google credential
                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                val authResult = FirebaseAuth.getInstance().signInWithCredential(firebaseCredential).await()
                
                // Get Firebase ID Token
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    val firebaseIdToken = firebaseUser.getIdToken(true).await().token
                    if (firebaseIdToken != null) {
                        android.util.Log.d("LoginScreen", "Firebase ID Token received, sending to backend")
                        // Send Firebase ID Token to backend
                        viewModel.loginWithGoogle(firebaseIdToken)
                    } else {
                        googleError = "Gagal mendapatkan Firebase token"
                    }
                } else {
                    googleError = "Gagal login ke Firebase"
                }
                
            } catch (e: GetCredentialCancellationException) {
                // User cancelled the sign-in
                android.util.Log.d("LoginScreen", "Google Sign-In cancelled by user")
                googleError = null // Don't show error for cancellation
            } catch (e: Exception) {
                android.util.Log.e("LoginScreen", "Google Sign-In failed", e)
                googleError = e.message ?: "Google Sign-In gagal"
            } finally {
                isGoogleLoading = false
            }
        }
    }

    LoginScreenContent(
        uiState = uiState,
        onLoginClick = viewModel::login,
        onClearError = viewModel::clearError,
        onRegisterClick = onNavigateToRegister,
        onGoogleSignInClick = onGoogleSignInClick,
        isGoogleLoading = isGoogleLoading,
        googleError = googleError
    )
}

@Composable
fun LoginScreenContent(
    uiState: LoginUiState,
    onLoginClick: (String, String) -> Unit,
    onClearError: () -> Unit,
    onRegisterClick: () -> Unit,
    onGoogleSignInClick: () -> Unit = {},
    isGoogleLoading: Boolean = false,
    googleError: String? = null
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Background Container
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // Upper Section with Modern Gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            AuthGradientStart,
                            AuthPrimaryColor,
                            AuthGradientEnd
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
                    radius = size.width * 0.4f,
                    center = Offset(size.width * 0.9f, size.height * 0.2f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.08f),
                    radius = size.width * 0.3f,
                    center = Offset(size.width * 0.1f, size.height * 0.7f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.03f),
                    radius = size.width * 0.25f,
                    center = Offset(size.width * 0.5f, size.height * -0.1f)
                )
            }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 56.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo Container with glow effect
                Box(
                    modifier = Modifier
                        .size(110.dp)
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
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Text(
                    text = "Selamat Datang",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
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
                .padding(top = 280.dp),
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
                
                androidx.compose.animation.AnimatedVisibility(
                    visible = uiState.error != null,
                    enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
                    exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically()
                ) {
                    Surface(
                        color = Color(0xFFFDE8E8),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f)),
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
                    label = { Text(stringResource(R.string.username_label)) },
                    placeholder = { Text(stringResource(R.string.username_placeholder)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = if (usernameError != null) MaterialTheme.colorScheme.error else AuthSecondaryColor
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AuthSecondaryColor,
                        focusedLabelColor = AuthSecondaryColor,
                        cursorColor = AuthSecondaryColor,
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
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (passwordError != null) MaterialTheme.colorScheme.error else AuthSecondaryColor
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
                        focusedBorderColor = AuthSecondaryColor,
                        focusedLabelColor = AuthSecondaryColor,
                        cursorColor = AuthSecondaryColor,
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

                // Login Button with Gradient
                Button(
                    onClick = { onLoginClick(username, password) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(12.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AuthPrimaryColor,
                        disabledContainerColor = AuthPrimaryColor.copy(alpha = 0.5f)
                    ),
                    enabled = !uiState.isLoading && !isGoogleLoading
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

                Spacer(modifier = Modifier.height(16.dp))

                // Divider with "atau"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = Color.LightGray
                    )
                    Text(
                        text = "atau",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = Color.LightGray
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Google Sign-In Error
                androidx.compose.animation.AnimatedVisibility(
                    visible = googleError != null,
                    enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
                    exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically()
                ) {
                    Surface(
                        color = Color(0xFFFDE8E8),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = googleError ?: "",
                                color = Color(0xFFEF4444),
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // Google Sign-In Button
                OutlinedButton(
                    onClick = onGoogleSignInClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Color.LightGray
                    ),
                    enabled = !uiState.isLoading && !isGoogleLoading
                ) {
                    if (isGoogleLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = AuthSecondaryColor,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            // Google Logo
                            Image(
                                painter = painterResource(id = R.drawable.ic_google),
                                contentDescription = "Google Logo",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Masuk dengan Google",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.DarkGray
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
                        text = stringResource(R.string.no_account),
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    TextButton(onClick = onRegisterClick) {
                        Text(
                            text = stringResource(R.string.register_button),
                            color = AuthSecondaryColor,
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
