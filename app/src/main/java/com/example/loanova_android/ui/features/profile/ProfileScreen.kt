package com.example.loanova_android.ui.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.loanova_android.data.model.dto.UserProfileResponse
import com.example.loanova_android.ui.theme.LoanovaBlue
import com.example.loanova_android.ui.theme.LoanovaGold
import com.example.loanova_android.ui.theme.LoanovaLightBlue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.example.loanova_android.BuildConfig

/**
 * Halaman Konten Profil.
 */
@Composable
fun ProfileScreen(
    padding: PaddingValues,
    onLogout: () -> Unit, // Still needed to trigger parent logout if needed, OR VM handles it? 
    // Ideally VM handles logout logic, but navigation to Login might need callback. 
    // For now, let's keep onLogout callback for navigation side effect if needed.
    // However, ProfileViewModel handles logout logic (clearing session). 
    // We should observe isLoggedIn from ProfileVM or just use callback. 
    // Let's use callback for now to match HomeScreen structure.
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (uiState.isLoading) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = LoanovaBlue)
            }
        } else if (uiState.isProfileNotFound) {
            // Empty State
            EmptyProfileState(username = uiState.username, onLogout = { viewModel.logout(); onLogout() })
        } else if (uiState.userProfile != null) {
            // Filled State
            FullProfileState(profile = uiState.userProfile, onLogout = { viewModel.logout(); onLogout() })
        } else {
            // Fallback / Error
             Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                 Column(horizontalAlignment = Alignment.CenterHorizontally) {
                     Text("Gagal memuat profil", color = Color.Gray)
                     if (uiState.error != null) {
                        Text(uiState.error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                     }
                     Spacer(modifier = Modifier.height(16.dp))
                     Button(onClick = { viewModel.fetchUserProfile() }) { Text("Coba Lagi") }
                     Spacer(modifier = Modifier.height(16.dp))
                     Button(onClick = { viewModel.logout(); onLogout() }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Logout") }
                 }
             }
        }
    }
}

@Composable
fun EmptyProfileState(username: String?, onLogout: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = LoanovaGold,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Profil Belum Lengkap",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = LoanovaBlue
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Halo ${username ?: "User"}, silakan lengkapi data diri Anda untuk mengajukan pinjaman.",
            textAlign = TextAlign.Center,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { /* TODO: Navigate to Complete Profile */ },
            colors = ButtonDefaults.buttonColors(containerColor = LoanovaBlue),
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Lengkapi Profil Sekarang")
        }
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onLogout) {
            Text("Keluar (Logout)", color = MaterialTheme.colorScheme.error)
        }
    }
}

// ...

@Composable
fun FullProfileState(profile: UserProfileResponse, onLogout: () -> Unit) {
    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        item {
             // Header Avatar
             Box(
                modifier = Modifier
                    .size(120.dp) // Slightly larger
                    .clip(CircleShape)
                    .background(LoanovaLightBlue),
                contentAlignment = Alignment.Center
            ) {
                if (profile.profilePhoto != null) {
                    AsyncImage(
                        model = getImageUrl(profile.profilePhoto),
                        contentDescription = "Foto Profil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = profile.fullName.take(1).uppercase(),
                        style = MaterialTheme.typography.displayMedium,
                        color = LoanovaBlue,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = profile.fullName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = LoanovaBlue
            )
            Text(
                text = "NIK: ${profile.nik}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            // Details Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ProfileDetailRow("Email", profile.username) // Username is email in this system? Or just username.
                    ProfileDetailRow("Username", profile.username)
                    ProfileDetailRow("No. Telepon", profile.phoneNumber)
                    ProfileDetailRow("Tanggal Lahir", profile.birthDate)
                    ProfileDetailRow("Alamat", profile.userAddress)
                    if (profile.npwpNumber != null) {
                        ProfileDetailRow("NPWP", profile.npwpNumber)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            // Document Photos Section
            Text(
                text = "Dokumen Pendukung",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = LoanovaBlue,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                textAlign = TextAlign.Start
            )
            
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (profile.ktpPhoto != null) {
                        Text("Foto KTP", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        AsyncImage(
                            model = getImageUrl(profile.ktpPhoto),
                            contentDescription = "Foto KTP",
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.LightGray)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    if (profile.npwpPhoto != null) {
                        Text("Foto NPWP", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        AsyncImage(
                            model = getImageUrl(profile.npwpPhoto),
                            contentDescription = "Foto NPWP",
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp) // Adjust height as needed
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.LightGray)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        item {
             Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Keluar (Logout)")
            }
        }
    }
}

fun getImageUrl(path: String?): String? {
    if (path.isNullOrEmpty()) return null
    // Remove leading slash if present to avoid double slash with base url trailing slash
    val cleanPath = if (path.startsWith("/")) path.substring(1) else path
    val url = "${BuildConfig.BASE_URL}uploads/$cleanPath"
     // Note: Backend maps /uploads/** to the upload directory (WebConfig.java).
     // JSON returns relative path e.g. "ktp/filename.jpg", so we prepend "uploads/".
     return "${BuildConfig.BASE_URL}uploads/$cleanPath"
}

@Composable
fun ProfileDetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f), modifier = Modifier.padding(top = 8.dp))
    }
}
