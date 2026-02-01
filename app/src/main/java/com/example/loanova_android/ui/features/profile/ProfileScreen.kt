package com.example.loanova_android.ui.features.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.twotone.AssignmentInd
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
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
 * Halaman Konten Profil dengan Desain Modern Premium
 * Features:
 * - Gradient Header with Decorative Elements
 * - Floating Profile Card with Avatar
 * - Quick Stats Section
 * - Modern Tab Navigation
 * - Glass-morphism Effects
 */
@Composable
fun ProfileScreen(
    padding: PaddingValues,
    onLogout: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToCompleteProfile: () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToChangePassword: () -> Unit = {},
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
            .background(Color(0xFFF5F7FA))
            .padding(bottom = padding.calculateBottomPadding())
    ) {
        if (uiState.isLoading) {
            ModernLoadingState()
        } else if (uiState.isProfileNotFound) {
            ModernEmptyProfileState(
                username = uiState.username, 
                onLogout = { viewModel.logout(); onLogout() },
                onNavigateToCompleteProfile = onNavigateToCompleteProfile
            )
        } else if (uiState.userProfile != null) {
            ModernFullProfileState(
                profile = uiState.userProfile, 
                onLogout = { viewModel.logout(); onLogout() },
                onNavigateBack = onNavigateBack,
                onEditProfile = onNavigateToEditProfile,
                onChangePassword = onNavigateToChangePassword
            )
        } else {
            ModernErrorState(
                error = uiState.error,
                onRetry = { viewModel.fetchUserProfile() },
                onLogout = { viewModel.logout(); onLogout() }
            )
        }
    }
}

// Primary color palette for profile
private val ProfilePrimaryColor = Color(0xFF1E3A5F) // Deep Navy Blue
private val ProfileSecondaryColor = Color(0xFF3B82F6) // Bright Blue
private val ProfileAccentColor = Color(0xFF10B981) // Emerald Green
private val ProfileGoldColor = Color(0xFFFFB800) // Gold

@Composable
fun ModernLoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                color = ProfileSecondaryColor,
                strokeWidth = 3.dp,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Memuat profil...",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernFullProfileState(
    profile: UserProfileResponse, 
    onLogout: () -> Unit,
    onNavigateBack: () -> Unit,
    onEditProfile: () -> Unit,
    onChangePassword: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header with Gradient Background
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                // Gradient Background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    ProfilePrimaryColor,
                                    Color(0xFF2D5A87),
                                    ProfileSecondaryColor
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
                            radius = size.width * 0.25f,
                            center = Offset(size.width * 0.1f, size.height * 0.8f)
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.03f),
                            radius = size.width * 0.3f,
                            center = Offset(size.width * 0.5f, size.height * -0.1f)
                        )
                    }
                    
                    // Top Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                            .statusBarsPadding(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                        
                        Text(
                            "Profil Saya",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = onChangePassword,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.15f))
                            ) {
                                Icon(
                                    Icons.Outlined.Lock,
                                    contentDescription = "Change Password",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            IconButton(
                                onClick = onLogout,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEF4444))
                                    .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = "Logout",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
                
                // Profile Card (Floating)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .align(Alignment.BottomCenter)
                        .offset(y = 35.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar with Ring
                        Box(contentAlignment = Alignment.BottomEnd) {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(ProfileSecondaryColor, ProfileAccentColor)
                                        )
                                    )
                                    .padding(3.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .padding(2.dp)
                                    .clip(CircleShape)
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
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.linearGradient(
                                                    colors = listOf(
                                                        ProfileSecondaryColor.copy(alpha = 0.2f),
                                                        ProfileAccentColor.copy(alpha = 0.2f)
                                                    )
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = profile.fullName.take(1).uppercase(),
                                            fontSize = 36.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ProfileSecondaryColor
                                        )
                                    }
                                }
                            }
                            
                            // Verified Badge
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(ProfileGoldColor)
                                    .border(2.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = profile.fullName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = ProfileAccentColor.copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = ProfileAccentColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "@${profile.username}",
                                    fontSize = 12.sp,
                                    color = ProfileAccentColor,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Quick Stats Section
        item {
            Spacer(modifier = Modifier.height(56.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickStatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Edit,
                    label = "Edit Profil",
                    color = ProfileSecondaryColor,
                    onClick = onEditProfile
                )
                QuickStatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Lock,
                    label = "Keamanan",
                    color = Color(0xFFFF9800),
                    onClick = onChangePassword
                )
                QuickStatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.CheckCircle,
                    label = "Terverifikasi",
                    color = ProfileAccentColor,
                    onClick = { }
                )
            }
        }
        
        // Tab Section
        item {
            Spacer(modifier = Modifier.height(24.dp))
            ModernTabBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
        
        // Tab Content
        item {
            Spacer(modifier = Modifier.height(16.dp))
            AnimatedVisibility(
                visible = selectedTab == 0,
                enter = fadeIn() + slideInVertically { -20 },
                exit = fadeOut() + slideOutVertically { -20 }
            ) {
                ModernDataDiriSection(profile)
            }
        }
        
        item {
            AnimatedVisibility(
                visible = selectedTab == 1,
                enter = fadeIn() + slideInVertically { -20 },
                exit = fadeOut() + slideOutVertically { -20 }
            ) {
                ModernDokumenSection(profile)
            }
        }
        
        // Bottom Spacing
        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun QuickStatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(90.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ModernTabBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabs = listOf(
        "Data Diri" to Icons.Outlined.Person,
        "Dokumen" to Icons.Outlined.Description
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
        ) {
            tabs.forEachIndexed { index, (title, icon) ->
                val isSelected = selectedTab == index
                val backgroundColor by animateColorAsState(
                    targetValue = if (isSelected) ProfileSecondaryColor else Color.Transparent,
                    animationSpec = tween(200),
                    label = "tabBg"
                )
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) Color.White else Color.Gray,
                    animationSpec = tween(200),
                    label = "tabText"
                )
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(backgroundColor)
                        .clickable { onTabSelected(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = textColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            title,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp,
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModernDataDiriSection(profile: UserProfileResponse) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Personal Info Card
        ModernInfoCard(
            title = "Informasi Pribadi",
            icon = Icons.Outlined.Person,
            iconColor = ProfileSecondaryColor
        ) {
            ModernInfoRow(
                icon = Icons.Outlined.Badge,
                label = "Nama Lengkap",
                value = profile.fullName,
                iconColor = Color(0xFF6366F1)
            )
            ModernInfoRow(
                icon = Icons.Outlined.Phone,
                label = "Nomor Telepon",
                value = profile.phoneNumber,
                iconColor = ProfileAccentColor
            )
            ModernInfoRow(
                icon = Icons.Outlined.Cake,
                label = "Tanggal Lahir",
                value = profile.birthDate,
                iconColor = Color(0xFFEC4899)
            )
        }
        
        // Identity Card
        ModernInfoCard(
            title = "Identitas",
            icon = Icons.Outlined.CreditCard,
            iconColor = Color(0xFFFF9800)
        ) {
            ModernInfoRow(
                icon = Icons.Outlined.CreditCard,
                label = "NIK",
                value = profile.nik,
                iconColor = Color(0xFF8B5CF6)
            )
            if (!profile.npwpNumber.isNullOrEmpty()) {
                ModernInfoRow(
                    icon = Icons.Outlined.Description,
                    label = "NPWP",
                    value = profile.npwpNumber,
                    iconColor = Color(0xFF14B8A6)
                )
            }
        }
        
        // Address Card
        ModernInfoCard(
            title = "Alamat",
            icon = Icons.Outlined.Home,
            iconColor = Color(0xFFEF4444)
        ) {
            ModernInfoRow(
                icon = Icons.Outlined.LocationOn,
                label = "Alamat Lengkap",
                value = profile.userAddress,
                iconColor = Color(0xFFF97316),
                isMultiLine = true
            )
        }
    }
}

@Composable
private fun ModernInfoCard(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black.copy(alpha = 0.8f)
                )
            }
            
            HorizontalDivider(
                color = Color.LightGray.copy(alpha = 0.3f),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            content()
        }
    }
}

@Composable
private fun ModernInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    iconColor: Color,
    isMultiLine: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = if (isMultiLine) Alignment.Top else Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                fontSize = 11.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                value,
                fontSize = 14.sp,
                color = Color.Black.copy(alpha = 0.85f),
                fontWeight = FontWeight.SemiBold,
                lineHeight = if (isMultiLine) 20.sp else 14.sp
            )
        }
    }
}

@Composable
private fun ModernDokumenSection(profile: UserProfileResponse) {
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }

    if (selectedImageUrl != null) {
        ModernImageViewerDialog(
            imageUrl = selectedImageUrl!!,
            onDismiss = { selectedImageUrl = null }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (profile.ktpPhoto != null) {
            ModernDocumentCard(
                title = "KTP",
                subtitle = "Kartu Tanda Penduduk",
                icon = "🪪",
                imageUrl = getImageUrl(profile.ktpPhoto),
                gradientColors = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)),
                onClick = { selectedImageUrl = getImageUrl(profile.ktpPhoto) }
            )
        }
        
        if (profile.npwpPhoto != null) {
            ModernDocumentCard(
                title = "NPWP",
                subtitle = "Nomor Pokok Wajib Pajak",
                icon = "📋",
                imageUrl = getImageUrl(profile.npwpPhoto),
                gradientColors = listOf(Color(0xFF14B8A6), Color(0xFF10B981)),
                onClick = { selectedImageUrl = getImageUrl(profile.npwpPhoto) }
            )
        }
        
        if (profile.ktpPhoto == null && profile.npwpPhoto == null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("📁", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Belum Ada Dokumen",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Dokumen identitas Anda belum diunggah",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun ModernDocumentCard(
    title: String,
    subtitle: String,
    icon: String,
    imageUrl: String?,
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Document Preview
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.LightGray.copy(alpha = 0.3f))
            ) {
                if (imageUrl != null) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                // Overlay gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.3f)
                                )
                            )
                        )
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(icon, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black.copy(alpha = 0.85f)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    subtitle,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Status badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ProfileAccentColor.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = ProfileAccentColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Terverifikasi",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = ProfileAccentColor
                        )
                    }
                }
            }
            
            // View Button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(colors = gradientColors)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Visibility,
                    contentDescription = "Lihat",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ModernImageViewerDialog(imageUrl: String, onDismiss: () -> Unit) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(ProfilePrimaryColor, ProfileSecondaryColor)
                            )
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Image,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Lihat Dokumen",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
                
                // Image
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Full Image",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun ModernErrorState(error: String?, onRetry: () -> Unit, onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color(0xFFFEE2E2)),
            contentAlignment = Alignment.Center
        ) {
            Text("😔", fontSize = 48.sp)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            "Gagal Memuat Profil",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black.copy(alpha = 0.8f)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            error ?: "Terjadi kesalahan saat memuat data profil Anda",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onRetry,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ProfileSecondaryColor)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Coba Lagi", fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        TextButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Logout",
                color = Color(0xFFEF4444),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ModernEmptyProfileState(
    username: String?,
    onLogout: () -> Unit,
    onNavigateToCompleteProfile: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Illustration
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            ProfileSecondaryColor.copy(alpha = 0.1f),
                            ProfileAccentColor.copy(alpha = 0.1f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                ProfileSecondaryColor.copy(alpha = 0.2f),
                                ProfileAccentColor.copy(alpha = 0.2f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.TwoTone.AssignmentInd,
                    contentDescription = null,
                    tint = ProfileSecondaryColor,
                    modifier = Modifier.size(60.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            "Profil Belum Lengkap",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black.copy(alpha = 0.85f)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            "Halo ${username ?: "User"} 👋",
            fontSize = 16.sp,
            color = ProfileSecondaryColor,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            "Lengkapi data diri Anda untuk menikmati\nsemua layanan pinjaman Loanova",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // Complete Profile Button
        Button(
            onClick = onNavigateToCompleteProfile,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ProfileSecondaryColor
            )
        ) {
            Icon(
                Icons.Default.PersonAdd,
                contentDescription = null,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "Lengkapi Profil Sekarang",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Benefits List
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "✨ Keuntungan melengkapi profil:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                BenefitItem("Akses ke semua jenis plafond")
                BenefitItem("Proses pengajuan lebih cepat")
                BenefitItem("Limit pinjaman lebih tinggi")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        TextButton(onClick = onLogout) {
            Text(
                "Logout",
                color = Color(0xFFEF4444),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun BenefitItem(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            tint = ProfileAccentColor,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text,
            fontSize = 12.sp,
            color = Color.Black.copy(alpha = 0.7f)
        )
    }
}

// Keep legacy composables for compatibility but mark them as deprecated
@Deprecated("Use ModernFullProfileState instead")
@Composable
fun FullProfileState(
    profile: UserProfileResponse, 
    onLogout: () -> Unit,
    onNavigateBack: () -> Unit,
    onEditProfile: () -> Unit,
    onChangePassword: () -> Unit
) {
    ModernFullProfileState(profile, onLogout, onNavigateBack, onEditProfile, onChangePassword)
}

@Deprecated("Use ModernErrorState instead")
@Composable
fun ErrorState(error: String?, onRetry: () -> Unit, onLogout: () -> Unit) {
    ModernErrorState(error, onRetry, onLogout)
}

@Deprecated("Use ModernEmptyProfileState instead")
@Composable
fun EmptyProfileState(username: String?, onLogout: () -> Unit, onNavigateToCompleteProfile: () -> Unit) {
    ModernEmptyProfileState(username, onLogout, onNavigateToCompleteProfile)
}

fun getImageUrl(path: String?): String? {
    if (path.isNullOrEmpty()) return null
    val cleanPath = if (path.startsWith("/")) path.substring(1) else path
    return "${BuildConfig.BASE_URL}uploads/$cleanPath"
}
