package com.example.loanova_android.ui.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.twotone.AssignmentInd
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.loanova_android.BuildConfig
import com.example.loanova_android.data.model.dto.UserProfileResponse
import com.example.loanova_android.ui.theme.LoanovaBlue
import com.example.loanova_android.ui.theme.LoanovaGold
import com.example.loanova_android.ui.theme.LoanovaLightBlue

/**
 * Halaman Konten Profil dengan Desain Finansial Modern.
 */
@Composable
fun ProfileScreen(
    padding: PaddingValues,
    onLogout: () -> Unit,
    onNavigateToCompleteProfile: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsState().value
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current

    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.fetchUserProfile()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // Base background
            .padding(padding)
    ) {
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = LoanovaBlue)
            }
        } else if (uiState.isProfileNotFound) {
            EmptyProfileState(
                username = uiState.username, 
                onLogout = { viewModel.logout(); onLogout() },
                onNavigateToCompleteProfile = onNavigateToCompleteProfile
            )
        } else if (uiState.userProfile != null) {
            FullProfileState(profile = uiState.userProfile, onLogout = { viewModel.logout(); onLogout() })
        } else {
            // Error State
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Gagal memuat profil", color = Color.Gray, style = MaterialTheme.typography.bodyLarge)
                if (uiState.error != null) {
                    Text(uiState.error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = 32.dp), textAlign = TextAlign.Center)
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { viewModel.fetchUserProfile() },
                    colors = ButtonDefaults.buttonColors(containerColor = LoanovaBlue)
                ) { Text("Coba Lagi") }
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = { viewModel.logout(); onLogout() }) { Text("Logout", color = MaterialTheme.colorScheme.error) }
            }
        }
    }
}

@Composable
fun EmptyProfileState(
    username: String?, 
    onLogout: () -> Unit,
    onNavigateToCompleteProfile: () -> Unit
) {
    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(32.dp))
            // Illustration Area
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(LoanovaLightBlue.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(LoanovaLightBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.TwoTone.AssignmentInd, // Or PersonAdd if unavailable
                        contentDescription = null,
                        tint = LoanovaBlue,
                        modifier = Modifier.size(60.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Yuk, Lengkapi Profilmu!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = LoanovaBlue,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Halo ${username ?: "User"}, data diri yang lengkap diperlukan untuk verifikasi keamanan dan membuka akses limit pinjaman Anda.",
                textAlign = TextAlign.Center,
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Benefits List
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.Start
            ) {
                BenefitItem("Pencairan dana lebih cepat")
                BenefitItem("Pengajuan 100% online")
                BenefitItem("Keamanan data terjamin 100%")
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            Button(
                onClick = onNavigateToCompleteProfile,
                colors = ButtonDefaults.buttonColors(containerColor = LoanovaBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp), // Taller button
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(4.dp)
            ) {
                Text("Lengkapi Profil Sekarang", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(20.dp))
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            TextButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Keluar (Logout)", color = MaterialTheme.colorScheme.error)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun BenefitItem(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 6.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = LoanovaGold, // Gold checkmarks for premium feel
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.DarkGray,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun FullProfileState(profile: UserProfileResponse, onLogout: () -> Unit) {
    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        // Header Section with Gradient Background
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            ) {
                // Background shape
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(LoanovaBlue, LoanovaLightBlue)
                            ),
                            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                        )
                )

                // Profile Content
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 0.dp) // Adjust if needed
                ) {
                    // Avatar with border
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color.White) // White border effect
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(LoanovaLightBlue), // Fallback bg
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
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = profile.fullName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black // Or LoanovaBlue if preferred
                    )
                    Text(
                        text = "@${profile.username}", // Showing username (which looks like a handle or ID)
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            
            // Personal Information Section
            SectionHeader(title = "Informasi Pribadi")
            
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Mapping data:
                    // Username -> "Username"
                    // FullName -> "Nama Lengkap" (Already in header but good to have details)
                    // PhoneNumber -> "No. Telepon"
                    // UserAddress -> "Alamat"
                    // BirthDate -> "Tanggal Lahir"
                    // NIK -> "NIK"
                    // NPWP -> "NPWP"
                    
                    ProfileInfoRow(icon = Icons.Outlined.AccountCircle, label = "Username", value = profile.username)
                    ProfileDivider()
                    ProfileInfoRow(icon = Icons.Outlined.Person, label = "Nama Lengkap", value = profile.fullName)
                    ProfileDivider()
                    ProfileInfoRow(icon = Icons.Outlined.Phone, label = "No. Telepon", value = profile.phoneNumber)
                    ProfileDivider()
                    ProfileInfoRow(icon = Icons.Outlined.DateRange, label = "Tanggal Lahir", value = profile.birthDate)
                    ProfileDivider()
                    ProfileInfoRow(icon = Icons.Outlined.Home, label = "Alamat", value = profile.userAddress)
                    ProfileDivider()
                    ProfileInfoRow(icon = Icons.Outlined.CreditCard, label = "NIK", value = profile.nik)
                    if (profile.npwpNumber != null) {
                        ProfileDivider()
                        ProfileInfoRow(icon = Icons.Outlined.Description, label = "NPWP", value = profile.npwpNumber)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            SectionHeader(title = "Dokumen Pendukung")
            
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                if (profile.ktpPhoto != null) {
                     DocumentCard(title = "Foto KTP", imageUrl = getImageUrl(profile.ktpPhoto))
                     Spacer(modifier = Modifier.height(16.dp))
                }
                
                if (profile.npwpPhoto != null) {
                     DocumentCard(title = "Foto NPWP", imageUrl = getImageUrl(profile.npwpPhoto))
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        item {
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2), contentColor = Color(0xFFDC2626)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Keluar Aplikasi", fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = LoanovaBlue,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        textAlign = TextAlign.Start
    )
}

@Composable
fun ProfileInfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = LoanovaBlue,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = Color.Black)
        }
    }
}

@Composable
fun ProfileDivider() {
    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 1.dp)
}

@Composable
fun DocumentCard(title: String, imageUrl: String?) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = Color.Gray, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            AsyncImage(
                model = imageUrl,
                contentDescription = title,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray.copy(alpha = 0.5f))
            )
        }
    }
}

fun getImageUrl(path: String?): String? {
    if (path.isNullOrEmpty()) return null
    val cleanPath = if (path.startsWith("/")) path.substring(1) else path
    return "${BuildConfig.BASE_URL}uploads/$cleanPath"
}
