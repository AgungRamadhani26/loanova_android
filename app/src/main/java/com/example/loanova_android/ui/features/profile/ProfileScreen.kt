package com.example.loanova_android.ui.features.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.twotone.AssignmentInd
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.loanova_android.BuildConfig
import com.example.loanova_android.data.model.dto.UserProfileResponse
import com.example.loanova_android.ui.theme.LoanovaBlue
import com.example.loanova_android.ui.theme.LoanovaGold
import com.example.loanova_android.ui.theme.LoanovaLightBlue
import com.example.loanova_android.ui.theme.LoanovaBackground

/**
 * Halaman Konten Profil dengan Desain Moderen (Doctor App Style Adaptation).
 * Features:
 * - Clean White Background
 * - Centered Avatar
 * - "Hero" Status Card (Blue Gradient)
 * - Tabbed Content (Data Diri vs Dokumen)
 */
@Composable
fun ProfileScreen(
    padding: PaddingValues,
    onLogout: () -> Unit,
    onNavigateToCompleteProfile: () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsState().value
    val lifecycleOwner = LocalLifecycleOwner.current

    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
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
            .background(Color(0xFFF8F9FE)) // Very subtle cool gray/blueish white
            .padding(bottom = padding.calculateBottomPadding())
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
            FullProfileState(
                profile = uiState.userProfile, 
                onLogout = { viewModel.logout(); onLogout() },
                onEditProfile = onNavigateToEditProfile
            )
        } else {
             Box(modifier = Modifier.fillMaxSize()) {
                ErrorState(
                    error = uiState.error,
                    onRetry = { viewModel.fetchUserProfile() },
                    onLogout = { viewModel.logout(); onLogout() }
                )
             }
        }
    }
}

@Composable
fun FullProfileState(
    profile: UserProfileResponse, 
    onLogout: () -> Unit,
    onEditProfile: () -> Unit
) {
    // Top Bar (Custom)
    Column(modifier = Modifier.fillMaxSize()) {
        // Custom Top Bar Area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack, // Or Menu if it was a drawer
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(24.dp).clickable { /* Handle Nav Back if needed, or remove */ }
            )
            Text(
                text = "Profile",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Icon(
                imageVector = Icons.Outlined.Logout,
                contentDescription = "Logout",
                tint = Color(0xFFEF4444),
                modifier = Modifier.size(24.dp).clickable { onLogout() }
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // 1. Avatar Section
            item {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.White, CircleShape)
                        .shadow(4.dp, CircleShape)
                        .background(Color.White)
                ) {
                    if (profile.profilePhoto != null) {
                        AsyncImage(
                            model = getImageUrl(profile.profilePhoto),
                            contentDescription = "Foto Profil",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize().background(LoanovaLightBlue.copy(alpha=0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = profile.fullName.take(1).uppercase(),
                                style = MaterialTheme.typography.displayMedium,
                                color = LoanovaBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    // Rating/Status Badge (Mimicking the '4.5' star in reference)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = (-4).dp, y = (-4).dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(LoanovaGold)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("Verified", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 8.sp)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = profile.fullName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Member Loanova • ${profile.username}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(24.dp))
            }

            // 2. Hero Status Card (The Blue Gradient Card)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(100.dp),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent) // Use Box for gradient
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(LoanovaBlue, Color(0xFF42A5F5))
                                )
                            )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left Stat
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable { onEditProfile() }, // Clicking this edits profile? Or just visual. Let's make it visual only or open details.
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Edit", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                                        Text("Profil", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    }
                                }
                            }
                            
                            // Divider
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(40.dp)
                                    .background(Color.White.copy(alpha = 0.3f))
                            )
                            
                            // Right Stat
                            Box(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Status", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                                        Text("Aktif", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // 3. Tabs (Data Diri | Dokumen)
            item {
                var selectedTab by remember { mutableStateOf(0) }
                
                // Tab Selection Logic
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(50.dp)
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .padding(4.dp)
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        // Tab 1
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selectedTab == 0) LoanovaLightBlue.copy(alpha = 0.2f) else Color.Transparent)
                                .clickable { selectedTab = 0 },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Data Diri",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == 0) LoanovaBlue else Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                        
                        // Tab 2
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selectedTab == 1) LoanovaLightBlue.copy(alpha = 0.2f) else Color.Transparent)
                                .clickable { selectedTab = 1 },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Dokumen",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == 1) LoanovaBlue else Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                // Content Switcher
                AnimatedVisibility(visible = selectedTab == 0, enter = fadeIn(), exit = fadeOut()) {
                    DataDiriList(profile)
                }
                AnimatedVisibility(visible = selectedTab == 1, enter = fadeIn(), exit = fadeOut()) {
                    DokumenList(profile)
                }
            }
        }
    }
}

@Composable
fun DataDiriList(profile: UserProfileResponse) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        ModernDetailsCard {
            ModernRowItem(Icons.Outlined.Person, "Nama Lengkap", profile.fullName)
            ModernRowItem(Icons.Outlined.Phone, "Nomor Telepon", profile.phoneNumber)
            ModernRowItem(Icons.Outlined.Email, "Username", profile.username) // Using Username as email placeholder contextually if needed, or just username
            ModernRowItem(Icons.Outlined.Cake, "Tanggal Lahir", profile.birthDate)
            ModernRowItem(Icons.Outlined.CreditCard, "NIK", profile.nik)
            ModernRowItem(Icons.Outlined.Home, "Alamat", profile.userAddress)
            if (!profile.npwpNumber.isNullOrEmpty()) {
                ModernRowItem(Icons.Outlined.Description, "NPWP", profile.npwpNumber)
            }
        }
    }
}

@Composable
fun DokumenList(profile: UserProfileResponse) {
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }

    if (selectedImageUrl != null) {
        ImageViewerDialog(imageUrl = selectedImageUrl!!, onDismiss = { selectedImageUrl = null })
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
         if (profile.ktpPhoto != null) {
            ModernDocCard(
                title = "KTP", 
                subtitle = "Identitas Utama", 
                imageUrl = getImageUrl(profile.ktpPhoto),
                onClick = { selectedImageUrl = getImageUrl(profile.ktpPhoto) }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        if (profile.npwpPhoto != null) {
            ModernDocCard(
                title = "NPWP", 
                subtitle = "Identitas Pajak", 
                imageUrl = getImageUrl(profile.npwpPhoto),
                onClick = { selectedImageUrl = getImageUrl(profile.npwpPhoto) }
            )
        }
    }
}

@Composable
fun ModernDetailsCard(content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            content()
        }
    }
}

@Composable
fun ModernRowItem(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(Color(0xFFF0F5FF), CircleShape), // Very light blue bg
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = LoanovaBlue, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, style = MaterialTheme.typography.bodyMedium, color = Color.Black, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun ModernDocCard(title: String, subtitle: String, imageUrl: String?, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray)
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                Text(subtitle, fontSize = 12.sp, color = Color.Gray)
            }
            // View Button
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(LoanovaBlue, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Visibility, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun ImageViewerDialog(imageUrl: String, onDismiss: () -> Unit) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onDismiss() } // Click outside to dismiss
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Content Wrapper to keep Close button relative to Image
            Box(
                contentAlignment = Alignment.TopEnd
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(8.dp),
                    modifier = Modifier.padding(top = 12.dp, end = 12.dp) // Make room for the button overlap if desired, or just simple overlay
                ) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Full Image",
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Close Button - Now relative to the Card
                Box(
                    modifier = Modifier
                        .offset(x = 8.dp, y = (-8).dp) // Slight offset to float at corner
                        .size(36.dp)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        .clickable { onDismiss() }
                        .zIndex(2f),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

// ... ErrorState and EmptyProfileState omitted for brevity, reusing previous implementation logic ... 
// IMPORTANT: Need to include them to keep the file compilable. Using simplified versions.

@Composable
fun ErrorState(error: String?, onRetry: () -> Unit, onLogout: () -> Unit) {
     Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Gagal memuat profil", color = Color.Gray, style = MaterialTheme.typography.bodyLarge)
        if (error != null) {
            Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = 32.dp), textAlign = TextAlign.Center)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = LoanovaBlue)) { Text("Coba Lagi") }
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onLogout) { Text("Logout", color = MaterialTheme.colorScheme.error) }
    }
}

@Composable
fun EmptyProfileState(username: String?, onLogout: () -> Unit, onNavigateToCompleteProfile: () -> Unit) {
    LazyColumn(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize().padding(24.dp)) {
        item {
            Box(modifier = Modifier.size(160.dp).clip(CircleShape).background(Color.White).padding(8.dp).clip(CircleShape).background(LoanovaLightBlue.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                Icon(Icons.TwoTone.AssignmentInd, contentDescription = null, tint = LoanovaBlue, modifier = Modifier.size(80.dp))
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text("Profil Belum Lengkap", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = LoanovaBlue, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Halo ${username ?: "User"}, lengkapi data diri Anda untuk menikmati layanan pinjaman.", textAlign = TextAlign.Center, color = Color.Gray, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(40.dp))
            Button(onClick = onNavigateToCompleteProfile, colors = ButtonDefaults.buttonColors(containerColor = LoanovaBlue), modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) {
                Text("Lengkapi Profil Sekarang", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.padding(start = 8.dp).size(20.dp))
            }
             Spacer(modifier = Modifier.height(24.dp))
             TextButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) { Text("Logout", color = MaterialTheme.colorScheme.error) }
        }
    }
}

fun getImageUrl(path: String?): String? {
    if (path.isNullOrEmpty()) return null
    val cleanPath = if (path.startsWith("/")) path.substring(1) else path
    return "${BuildConfig.BASE_URL}uploads/$cleanPath"
}
