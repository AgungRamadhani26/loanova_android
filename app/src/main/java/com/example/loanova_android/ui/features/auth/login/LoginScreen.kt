package com.example.loanova_android.ui.features.auth.login

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

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onNavigateToDashboard: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val isPreview = LocalInspectionMode.current

    LaunchedEffect(uiState.success) {
        uiState.success?.let {
            onNavigateToDashboard(it.username)
        }
    }

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
                text = "Loanova",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = LoanovaBlue
            )
            Text(
                text = "Solusi Finansial Masa Depan",
                fontSize = 14.sp,
                color = Color.Gray.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(48.dp))

            if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
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
                    viewModel.clearError()
                },
                label = { Text("Username") },
                placeholder = { Text("Masukkan username Anda") },
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
                    viewModel.clearError()
                },
                label = { Text("Password") },
                placeholder = { Text("Masukkan password Anda") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = if (passwordVisible) "Sembunyikan password" else "Tampilkan password")
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
                onClick = { viewModel.login(username, password) },
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
                        text = "MASUK",
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
                Text(text = "Belum punya akun?", color = Color.Gray, fontSize = 14.sp)
                TextButton(onClick = { /* TODO: Register */ }) {
                    Text(text = "Daftar Sekarang", color = LoanovaBlue, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
