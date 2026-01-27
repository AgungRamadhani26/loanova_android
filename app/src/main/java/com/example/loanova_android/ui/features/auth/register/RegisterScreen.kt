package com.example.loanova_android.ui.features.auth.register

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
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
                    Text("Login Sekarang")
                }
            },
            title = { Text("Pendaftaran Berhasil") },
            text = { Text("Akun Anda telah berhasil dibuat. Silakan login untuk melanjutkan.") },
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Green) }
        )
    }

    Scaffold(
        containerColor = LoanovaBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Daftar Akun",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = LoanovaBlue
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Lengkapi data diri Anda untuk memulai",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            RegisterForm(
                isLoading = uiState.isLoading,
                error = uiState.error,
                onRegister = viewModel::register,
                onBack = onNavigateToLogin
            )
        }
    }
}

@Composable
fun RegisterForm(
    isLoading: Boolean,
    error: String?,
    onRegister: (RegisterRequest) -> Unit,
    onBack: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    if (error != null) {
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center
            )
        }
    }

    OutlinedTextField(
        value = username,
        onValueChange = { username = it },
        label = { Text("Username") },
        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = LoanovaBlue) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        enabled = !isLoading
    )
    
    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = email,
        onValueChange = { email = it },
        label = { Text("Email") },
        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = LoanovaBlue) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        enabled = !isLoading
    )

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = password,
        onValueChange = { password = it },
        label = { Text("Password") },
        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = LoanovaBlue) },
        trailingIcon = {
            val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(imageVector = image, contentDescription = null)
            }
        },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        enabled = !isLoading
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "Password harus mengandung huruf besar, kecil, angka, dan simbol.",
        style = MaterialTheme.typography.bodySmall,
        color = Color.Gray,
        fontSize = 12.sp
    )

    Spacer(modifier = Modifier.height(32.dp))

    Button(
        onClick = { 
            onRegister(RegisterRequest(username, email, password)) 
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = LoanovaBlue),
        enabled = !isLoading && username.isNotBlank() && email.isNotBlank() && password.isNotBlank()
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
        } else {
            Text("Daftar Sekarang", fontWeight = FontWeight.Bold)
        }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    TextButton(onClick = onBack, enabled = !isLoading, modifier = Modifier.fillMaxWidth()) {
        Text("Sudah punya akun? Masuk", color = LoanovaBlue)
    }
}
