package com.example.loanova_android.ui.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.loanova_android.domain.model.Plafond
import com.example.loanova_android.data.model.dto.UserProfileResponse
import com.example.loanova_android.ui.theme.*
import com.example.loanova_android.ui.features.profile.ProfileScreen
import java.text.NumberFormat
import java.util.Locale

@Composable
fun HomeScreen(
    onNavigateToLogin: () -> Unit = {},
    onNavigateToCompleteProfile: () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToChangePassword: () -> Unit = {},
    onNavigateToActivePlafond: () -> Unit = {}, // New callback
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Check login status on resume/composition
    LaunchedEffect(Unit) {
        viewModel.checkLoginStatus()
    }

    var selectedTab by rememberSaveable { mutableIntStateOf(0) } // 0: Home

    // Reset tab to Home when logged out
    LaunchedEffect(uiState.isLoggedIn) {
        if (!uiState.isLoggedIn && selectedTab != 0) {
            selectedTab = 0
        }
    }

    Scaffold(

        bottomBar = {
            LoanovaBottomNavigation(
                selectedTab = selectedTab,
                onTabSelected = { index ->
                    if (index == 0) {
                        selectedTab = index
                    } else {
                        // Restricted tabs
                        if (uiState.isLoggedIn) {
                            selectedTab = index
                        } else {
                            onNavigateToLogin()
                        }
                    }
                }
            )
        },
        containerColor = LoanovaBackground
    ) { padding ->
        // Content based on tab
        when (selectedTab) {
            0 -> HomeContent(
                padding = padding,
                uiState = uiState,
                onNavigateToLogin = onNavigateToLogin,
                onNavigateToActivePlafond = onNavigateToActivePlafond
            )

            3 -> ProfileScreen(
                padding = padding,
                onLogout = { 
                    viewModel.logout() 
                },
                onNavigateBack = { selectedTab = 0 },
                onNavigateToCompleteProfile = onNavigateToCompleteProfile,
                onNavigateToEditProfile = onNavigateToEditProfile,
                onNavigateToChangePassword = onNavigateToChangePassword
            )
            else -> Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Fitur Belum Tersedia / Placeholder")
            }
        }
    }
}

@Composable
fun ProfileDetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f), modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
fun HomeContent(
    padding: PaddingValues,
    uiState: HomeUiState,
    onNavigateToLogin: () -> Unit,
    onNavigateToActivePlafond: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item { HeroSection(onNavigateToLogin, uiState.isLoggedIn, uiState.username) }
        
        // Quick Menu
        item { 
            QuickMenuSection(
                onNavigateToLogin = onNavigateToLogin, 
                onNavigateToActivePlafond = onNavigateToActivePlafond,
                isLoggedIn = uiState.isLoggedIn
            ) 
        }
        
        item { PlafondTitleSection() }
        
        if (uiState.isLoading) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = LoanovaBlue)
                }
            }
        } else if (uiState.error != null) {
            item {
                Text(
                    text = "Gagal memuat data: ${uiState.error}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            item { PlafondListSection(uiState.plafonds) }
        }

        // Moved FeatureSection here and refined it
        // Moved FeatureSection here and refined it
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun LoanovaBottomNavigation(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        contentColor = LoanovaBlue,
        tonalElevation = 8.dp,
        modifier = Modifier.height(68.dp), // Reduced height to be less "upwards"
        windowInsets = WindowInsets(0.dp) // Reset insets to align lower
    ) {
        val items = listOf(
            "Beranda" to Icons.Default.Home,
            "Pinjaman" to Icons.Default.CreditCard,
            "Notifikasi" to Icons.Default.Notifications,
            "Profil" to Icons.Default.Person
        )

        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = { Icon(imageVector = item.second, contentDescription = item.first) },
                label = { Text(text = item.first, style = MaterialTheme.typography.labelSmall) },
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = LoanovaBlue,
                    selectedTextColor = LoanovaBlue,
                    indicatorColor = LoanovaBlue.copy(alpha = 0.1f),
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray
                )
            )
        }
    }
}

@Composable
fun QuickMenuSection(
    onNavigateToLogin: () -> Unit, 
    onNavigateToActivePlafond: () -> Unit,
    isLoggedIn: Boolean
) {
    val items = listOf(
        QuickMenuItem("Simulasi", Icons.Default.Calculate, Color(0xFF4CAF50)),
        QuickMenuItem("Ajukan", Icons.Default.CreditScore, Color(0xFF2196F3)),
        QuickMenuItem("Plafond", Icons.Default.AccountBalanceWallet, Color(0xFFFF9800)),
        QuickMenuItem("Riwayat", Icons.Default.History, Color(0xFF9C27B0))
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally)
    ) {
        items.forEach { item ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { 
                        if (item.label == "Plafond") {
                            if (isLoggedIn) {
                                onNavigateToActivePlafond()
                            } else {
                                onNavigateToLogin()
                            }
                        } else {
                            // Placeholder for other items
                            if (!isLoggedIn) onNavigateToLogin()
                        }
                    }
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(item.color.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = item.color,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    fontSize = 12.sp,
                    color = Color.Black.copy(alpha = 0.8f)
                )
            }
        }
    }
}

data class QuickMenuItem(val label: String, val icon: ImageVector, val color: Color)

@Composable
fun PlafondCard(plafond: Plafond, modifier: Modifier = Modifier) {
    val themeColor = getPlafondColor(plafond.name)
    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            themeColor,
            LoanovaBlue
        )
    )
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(gradientBrush)
        ) {
             // Background Decoration (Circles)
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.1f),
                    center = androidx.compose.ui.geometry.Offset(x = size.width, y = 0f),
                    radius = size.width * 0.5f
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.05f),
                    center = androidx.compose.ui.geometry.Offset(x = 0f, y = size.height),
                    radius = size.width * 0.4f
                )
            }

            Column(
                modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp) // Reduced padding
            ) {
                // Header
                Text(
                    text = plafond.name, 
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium, // Smaller title
                    fontSize = 14.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = plafond.description, 
                    style = MaterialTheme.typography.bodySmall, 
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    maxLines = 2,
                    minLines = 2,
                    lineHeight = 16.sp,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp))
                
                // Limit Info
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(
                            text = "Max Limit", 
                            style = MaterialTheme.typography.labelSmall, 
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = formatCurrency(plafond.maxAmount), 
                            fontWeight = FontWeight.Bold, 
                            fontSize = 14.sp, // Reduced font
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Interest Badge
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "Bunga ${plafond.interestRate}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp)) // Reduced spacer
                
                // Simulation Button (Styled white)
                Button(
                    onClick = { /* TODO: Navigate to Simulation */ },
                    modifier = Modifier.fillMaxWidth().height(32.dp), // Height 32dp
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = themeColor 
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Lakukan Simulasi", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}



fun getPlafondColor(name: String): Color {
    return when {
        name.contains("Gold", ignoreCase = true) -> Color(0xFFFFC107) // Amber 500 (Bright Gold)
        name.contains("Silver", ignoreCase = true) -> Color(0xFF9E9E9E) // Grey 500
        name.contains("Bronze", ignoreCase = true) -> Color(0xFFD84315) // Deep Orange 800
        name.contains("Platinum", ignoreCase = true) -> Color(0xFF00BCD4) // Cyan 500 (Diamond Blue)
        name.contains("Red", ignoreCase = true) -> Color(0xFFF44336) // Red 500
        name.contains("Black", ignoreCase = true) -> Color(0xFF212121) // Grey 900
        else -> LoanovaBlue
    }
}

fun formatCurrency(amount: java.math.BigDecimal): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    format.maximumFractionDigits = 0 // Remove decimals
    return format.format(amount).replace("Rp", "Rp ")
}



@Composable
fun HeroSection(onNavigateToLogin: () -> Unit, isLoggedIn: Boolean, username: String? = null) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.horizontalGradient(listOf(LoanovaBlue, LoanovaLightBlue)))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "Selamat Datang di Loanova",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            // Brief explanation of advantages
            Text(
                text = "Solusi keuangan digital yang Cepat, Aman, dan 100% Online.",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Nikmati kemudahan akses finansial kapan saja dengan layanan Support 24/7 terpercaya.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f),
                lineHeight = 20.sp
            )

            if (!isLoggedIn) {
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onNavigateToLogin,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Masuk Sekarang", color = LoanovaBlue, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null, tint = LoanovaBlue)
                }
            }
        }
    }
}

@Composable
fun PlafondTitleSection() {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = "Pilihan Pinjaman",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Sesuaikan dengan kebutuhan Anda",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Composable
fun PlafondListSection(plafonds: List<Plafond>) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        if (plafonds.isEmpty()) {
             // Optional: Show empty state
        } else {
            plafonds.forEach { plafond ->
                PlafondCard(
                    plafond = plafond,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp) // Extra padding to make it narrower than Hero
                        .padding(bottom = 12.dp)
                )
            }
        }
    }
}

// FeatureSection, SecuritySection, StepsSection removed as per user request

