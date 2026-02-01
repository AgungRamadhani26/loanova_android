package com.example.loanova_android.ui.features.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.loanova_android.domain.model.Plafond
import com.example.loanova_android.data.model.dto.UserProfileResponse
import com.example.loanova_android.ui.theme.*
import com.example.loanova_android.ui.features.profile.ProfileScreen
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun HomeScreen(
    onNavigateToLogin: () -> Unit = {},
    onNavigateToCompleteProfile: () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToChangePassword: () -> Unit = {},
    onNavigateToActivePlafond: () -> Unit = {},
    onNavigateToLoanApplication: () -> Unit = {}, // New callback for loan application
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Check login status on resume/composition
    LaunchedEffect(Unit) {
        viewModel.checkLoginStatus()
    }

    var selectedTab by rememberSaveable { mutableIntStateOf(0) } // 0: Home
    var showProfileRequiredDialog by remember { mutableStateOf(false) }

    // Reset tab to Home when logged out
    LaunchedEffect(uiState.isLoggedIn) {
        if (!uiState.isLoggedIn && selectedTab != 0) {
            selectedTab = 0
        }
    }

    if (showProfileRequiredDialog) {
        RestrictedActionDialog(
            onDismiss = { showProfileRequiredDialog = false },
            onCompleteProfile = {
                showProfileRequiredDialog = false
                onNavigateToCompleteProfile()
            }
        )
    }

    Scaffold(

        bottomBar = {
            LoanovaBottomNavigation(
                selectedTab = selectedTab,
                onTabSelected = { index ->
                    if (index == 0) {
                        selectedTab = index
                    } else if (index == 1 || index == 2) { // 1: Pinjaman, 2: Notifikasi
                        if (!uiState.isLoggedIn) {
                            onNavigateToLogin()
                        } else if (!uiState.hasProfile) {
                            showProfileRequiredDialog = true
                        } else {
                            selectedTab = index
                        }
                    } else {
                        // Restricted tabs (Profil)
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
                onNavigateToActivePlafond = onNavigateToActivePlafond,
                onNavigateToLoanApplication = onNavigateToLoanApplication,
                onProfileRequired = { showProfileRequiredDialog = true }
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
    onNavigateToActivePlafond: () -> Unit,
    onNavigateToLoanApplication: () -> Unit,
    onProfileRequired: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Hero Section dengan Greeting
        item { HeroSection(onNavigateToLogin, uiState.isLoggedIn, uiState.username) }
        
        // Quick Menu dengan Glassmorphism
        item { 
            QuickMenuSection(
                onNavigateToLogin = onNavigateToLogin, 
                onNavigateToActivePlafond = onNavigateToActivePlafond,
                onNavigateToLoanApplication = onNavigateToLoanApplication,
                onProfileRequired = onProfileRequired,
                isLoggedIn = uiState.isLoggedIn,
                hasProfile = uiState.hasProfile,
                plafonds = uiState.plafonds
            ) 
        }
        
        // Promo Banner Section
        item { PromoBannerSection() }
        
        // Plafond Section dengan decorative header
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

        // Tips Keuangan Section
        item { FinancialTipsSection() }
        
        // Footer Section
        item { FooterSection() }
        
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
    onNavigateToLoanApplication: () -> Unit,
    onProfileRequired: () -> Unit,
    isLoggedIn: Boolean,
    hasProfile: Boolean,
    plafonds: List<Plafond>
) {
    var showSimulationDialog by remember { mutableStateOf(false) }
    
    val items = listOf(
        QuickMenuItem("Simulasi", Icons.Default.Calculate, Color(0xFF4CAF50)),
        QuickMenuItem("Ajukan", Icons.Default.CreditScore, Color(0xFF2196F3)),
        QuickMenuItem("Plafond", Icons.Default.AccountBalanceWallet, Color(0xFFFF9800)),
        QuickMenuItem("Riwayat", Icons.Default.History, Color(0xFF9C27B0))
    )
    
    // Show Standalone Simulation Dialog (no login required)
    if (showSimulationDialog) {
        StandaloneSimulationDialog(
            plafonds = plafonds,
            onDismiss = { showSimulationDialog = false }
        )
    }

    // Glassmorphism Card Container
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(vertical = 8.dp)
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            items.forEach { item ->
                QuickMenuItemCard(
                    item = item,
                    onClick = {
                        when (item.label) {
                            "Simulasi" -> {
                                // No login required for simulation
                                showSimulationDialog = true
                            }
                            "Plafond" -> {
                                if (!isLoggedIn) {
                                    onNavigateToLogin()
                                } else if (!hasProfile) {
                                    onProfileRequired()
                                } else {
                                    onNavigateToActivePlafond()
                                }
                            }
                            "Ajukan", "Riwayat" -> {
                                if (!isLoggedIn) {
                                    onNavigateToLogin()
                                } else if (!hasProfile) {
                                    onProfileRequired()
                                } else {
                                    if (item.label == "Ajukan") onNavigateToLoanApplication()
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun QuickMenuItemCard(
    item: QuickMenuItem,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        // Animated icon container
        Box(
            modifier = Modifier
                .size(50.dp)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            item.color.copy(alpha = 0.15f),
                            item.color.copy(alpha = 0.05f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            item.color.copy(alpha = 0.3f),
                            item.color.copy(alpha = 0.1f)
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = item.color,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            fontSize = 11.sp,
            color = Color.Black.copy(alpha = 0.8f)
        )
    }
}

data class QuickMenuItem(val label: String, val icon: ImageVector, val color: Color)

@Composable
fun PlafondCard(plafond: Plafond, onSimulateClick: () -> Unit, modifier: Modifier = Modifier) {
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
                    onClick = onSimulateClick,
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
    // Get time-based greeting
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour < 11 -> "Selamat Pagi" to "☀️"
            hour < 15 -> "Selamat Siang" to "🌤️"
            hour < 18 -> "Selamat Sore" to "🌅"
            else -> "Selamat Malam" to "🌙"
        }
    }
    
    // Floating animation
    val infiniteTransition = rememberInfiniteTransition(label = "hero")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(12.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF1A237E),
                        Color(0xFF3949AB),
                        LoanovaBlue
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
    ) {
        // Decorative floating circles
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.08f),
                center = Offset(size.width * 0.85f, size.height * 0.2f),
                radius = size.width * 0.35f
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.05f),
                center = Offset(size.width * 0.1f, size.height * 0.9f),
                radius = size.width * 0.25f
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.03f),
                center = Offset(size.width * 0.5f, size.height * 1.2f),
                radius = size.width * 0.4f
            )
        }
        
        Column(modifier = Modifier.padding(24.dp)) {
            // Greeting Row with emoji
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = greeting.second,
                    fontSize = 24.sp,
                    modifier = Modifier.graphicsLayer {
                        translationY = floatOffset
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = greeting.first,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )
                    if (isLoggedIn && username != null) {
                        Text(
                            text = username,
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // App Tagline
            Surface(
                color = Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Verified,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cepat • Aman • 100% Online",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Solusi keuangan digital terpercaya dengan proses mudah dan dukungan 24/7.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f),
                lineHeight = 20.sp
            )

            if (!isLoggedIn) {
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onNavigateToLogin,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text("Masuk Sekarang", color = Color(0xFF1A237E), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null, tint = Color(0xFF1A237E))
                }
            }
        }
    }
}

@Composable
fun PlafondTitleSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Decorative accent bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(32.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(LoanovaBlue, Color(0xFF4CAF50))
                        )
                    )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Pilihan Pinjaman",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF1A237E)
                )
                Text(
                    text = "Sesuaikan dengan kebutuhan Anda",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
        
        // Decorative badge
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = LoanovaBlue.copy(alpha = 0.1f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.LocalOffer,
                    contentDescription = null,
                    tint = LoanovaBlue,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Promo",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = LoanovaBlue
                )
            }
        }
    }
}

@Composable
fun PlafondListSection(plafonds: List<Plafond>) {
    var selectedPlafondForSimulation by remember { mutableStateOf<Plafond?>(null) }
    
    // Show Simulation Dialog
    selectedPlafondForSimulation?.let { plafond ->
        LoanSimulationDialog(
            plafond = plafond,
            onDismiss = { selectedPlafondForSimulation = null }
        )
    }
    
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        if (plafonds.isEmpty()) {
             // Optional: Show empty state
        } else {
            plafonds.forEach { plafond ->
                PlafondCard(
                    plafond = plafond,
                    onSimulateClick = { selectedPlafondForSimulation = plafond },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp) // Extra padding to make it narrower than Hero
                        .padding(bottom = 12.dp)
                )
            }
        }
    }
}

/**
 * Promo Banner Section - Horizontal scrollable promo cards
 */
@Composable
fun PromoBannerSection() {
    val promos = listOf(
        PromoItem(
            title = "Bunga Ringan",
            subtitle = "Mulai dari 0.75%/bulan",
            icon = "\ud83c\udf81",
            gradientColors = listOf(Color(0xFF667eea), Color(0xFF764ba2))
        ),
        PromoItem(
            title = "Proses Cepat",
            subtitle = "Approval 1x24 jam",
            icon = "\u26a1",
            gradientColors = listOf(Color(0xFFf093fb), Color(0xFFf5576c))
        ),
        PromoItem(
            title = "Tanpa Ribet",
            subtitle = "Untuk pinjaman tertentu",
            icon = "\ud83d\udee1\ufe0f",
            gradientColors = listOf(Color(0xFF4facfe), Color(0xFF00f2fe))
        )
    )
    
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Keunggulan Kami",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A237E)
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Horizontal scroll promos
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            promos.forEach { promo ->
                PromoBannerCard(promo = promo)
            }
        }
    }
}

data class PromoItem(
    val title: String,
    val subtitle: String,
    val icon: String,
    val gradientColors: List<Color>
)

@Composable
private fun PromoBannerCard(promo: PromoItem) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(90.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(colors = promo.gradientColors)
                )
        ) {
            // Decorative circle
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.1f),
                    center = Offset(size.width * 0.9f, size.height * 0.3f),
                    radius = size.width * 0.3f
                )
            }
            
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(promo.icon, fontSize = 28.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        promo.title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1
                    )
                    Text(
                        promo.subtitle,
                        fontSize = 9.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 2,
                        lineHeight = 11.sp
                    )
                }
            }
        }
    }
}

/**
 * Financial Tips Section - Horizontal scrollable tips cards
 */
@Composable
fun FinancialTipsSection() {
    var selectedTip by remember { mutableStateOf<Pair<TipItem, Int>?>(null) }
    
    val tips = listOf(
        TipItem(
            title = "Kelola Utang dengan Bijak",
            description = "Pastikan cicilan tidak melebihi 30% dari pendapatan bulanan",
            fullDescription = "Aturan 30% adalah panduan penting dalam mengelola utang. Artinya, total cicilan bulanan Anda (termasuk KPR, cicilan kendaraan, kartu kredit, dan pinjaman lainnya) sebaiknya tidak melebihi 30% dari penghasilan bersih bulanan.\n\nContoh: Jika gaji bersih Anda Rp 10.000.000, maka maksimal cicilan adalah Rp 3.000.000.\n\n✅ Tips:\n• Hitung semua cicilan yang ada\n• Pertimbangkan kebutuhan darurat\n• Sisakan dana untuk tabungan",
            icon = Icons.Outlined.Lightbulb,
            color = Color(0xFFFF9800)
        ),
        TipItem(
            title = "Dana Darurat",
            description = "Sisihkan 3-6 bulan pengeluaran sebagai dana darurat",
            fullDescription = "Dana darurat adalah simpanan yang disiapkan untuk situasi tidak terduga seperti kehilangan pekerjaan, sakit, atau kebutuhan mendesak lainnya.\n\nBerapa idealnya?\n• Single: 3-6 bulan pengeluaran\n• Menikah tanpa anak: 6-9 bulan\n• Menikah dengan anak: 9-12 bulan\n\n✅ Tips Membangun Dana Darurat:\n• Mulai dari 10% penghasilan\n• Simpan di rekening terpisah\n• Jangan digunakan untuk investasi berisiko\n• Tempatkan di instrumen likuid (tabungan/deposito)",
            icon = Icons.Outlined.Savings,
            color = Color(0xFF4CAF50)
        ),
        TipItem(
            title = "Bandingkan Sebelum Pinjam",
            description = "Perhatikan suku bunga, biaya admin, dan ketentuan lainnya",
            fullDescription = "Sebelum mengajukan pinjaman, bandingkan beberapa hal penting berikut:\n\n📊 Yang Harus Dibandingkan:\n• Suku bunga efektif per tahun\n• Biaya administrasi & provisi\n• Biaya penalti pelunasan dini\n• Asuransi yang disyaratkan\n• Fleksibilitas tenor\n\n⚠️ Waspadai:\n• Bunga flat vs bunga efektif\n• Biaya tersembunyi\n• Syarat dan ketentuan yang merugikan\n\n✅ Tips:\n• Gunakan kalkulator simulasi\n• Baca kontrak dengan teliti\n• Tanyakan total biaya keseluruhan",
            icon = Icons.Outlined.CompareArrows,
            color = Color(0xFF2196F3)
        ),
        TipItem(
            title = "Bayar Tepat Waktu",
            description = "Hindari denda keterlambatan dengan membayar sebelum jatuh tempo",
            fullDescription = "Membayar cicilan tepat waktu sangat penting untuk kesehatan keuangan Anda.\n\n❌ Dampak Telat Bayar:\n• Denda keterlambatan (biasanya 1-5% dari cicilan)\n• Bunga berjalan terus\n• Skor kredit menurun\n• Sulit mengajukan pinjaman di masa depan\n• Risiko penagihan\n\n✅ Tips Agar Selalu Tepat Waktu:\n• Pasang reminder H-3 jatuh tempo\n• Gunakan auto-debit\n• Bayar di awal bulan saat gaji masuk\n• Siapkan dana cadangan 1 bulan cicilan\n• Hubungi pemberi pinjaman jika kesulitan",
            icon = Icons.Outlined.AccessTime,
            color = Color(0xFF9C27B0)
        )
    )
    
    // Show Tip Detail Dialog
    selectedTip?.let { (tip, index) ->
        TipDetailDialog(
            tip = tip,
            index = index,
            onDismiss = { selectedTip = null }
        )
    }
    
    Column(
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
        // Section Header dengan decorative element
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFFFF3E0)),
                contentAlignment = Alignment.Center
            ) {
                Text("💡", fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Tips Keuangan",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A237E)
                )
                Text(
                    text = "Tap untuk baca selengkapnya",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Horizontal scrollable tips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(tips) { index, tip ->
                TipCard(
                    tip = tip, 
                    index = index,
                    onClick = { selectedTip = tip to index }
                )
            }
        }
    }
}

data class TipItem(
    val title: String,
    val description: String,
    val fullDescription: String = description,
    val icon: ImageVector,
    val color: Color
)

@Composable
private fun TipCard(tip: TipItem, index: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .height(130.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Top accent bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(tip.color)
            )
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .padding(top = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(tip.color.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                tip.icon,
                                contentDescription = null,
                                tint = tip.color,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Tips #${index + 1}",
                            fontSize = 9.sp,
                            color = tip.color,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    // Tap hint
                    Icon(
                        Icons.Default.TouchApp,
                        contentDescription = "Tap to read",
                        tint = Color.LightGray,
                        modifier = Modifier.size(14.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    tip.title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    tip.description,
                    fontSize = 10.sp,
                    color = Color.Gray,
                    lineHeight = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Read more hint
                Text(
                    "Tap untuk baca selengkapnya →",
                    fontSize = 9.sp,
                    color = tip.color,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Tip Detail Dialog - Shows full tip content
 */
@Composable
private fun TipDetailDialog(
    tip: TipItem,
    index: Int,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column {
                // Header with gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    tip.color,
                                    tip.color.copy(alpha = 0.7f)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    // Decorative circles
                    Canvas(modifier = Modifier.matchParentSize()) {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.1f),
                            center = Offset(size.width * 0.9f, size.height * 0.2f),
                            radius = size.width * 0.25f
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.05f),
                            center = Offset(size.width * 0.1f, size.height * 0.8f),
                            radius = size.width * 0.15f
                        )
                    }
                    
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        tip.icon,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "Tips #${index + 1}",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            
                            // Close button
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
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            tip.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            lineHeight = 22.sp
                        )
                    }
                }
                
                // Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        tip.fullDescription,
                        fontSize = 13.sp,
                        color = Color.Black.copy(alpha = 0.8f),
                        lineHeight = 20.sp
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Action button
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = tip.color)
                    ) {
                        Text(
                            "Mengerti",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Standalone Simulation Dialog - Multi-step simulation without login
 * Step 1: Select Plafond
 * Step 2: Input Amount & Tenor, Calculate
 * Design: Wizard style with emerald green theme (different from plafond-based dialog)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandaloneSimulationDialog(
    plafonds: List<Plafond>,
    onDismiss: () -> Unit
) {
    var currentStep by remember { mutableIntStateOf(1) }
    var selectedPlafond by remember { mutableStateOf<Plafond?>(null) }
    var amountText by remember { mutableStateOf("") }
    var tenor by remember { mutableIntStateOf(0) }
    var showResult by remember { mutableStateOf(false) }
    
    // Primary color for this dialog (Emerald Green - different from LoanSimulationDialog)
    val primaryColor = Color(0xFF059669) // Emerald 600
    val secondaryColor = Color(0xFF10B981) // Emerald 500
    val bgGradient = listOf(Color(0xFF064E3B), Color(0xFF059669)) // Dark to light emerald
    
    // Reset values when plafond changes
    LaunchedEffect(selectedPlafond) {
        selectedPlafond?.let {
            if (amountText.isEmpty()) amountText = "5000000"
            tenor = it.tenorMin
            showResult = false
        }
    }
    
    // Calculations
    val amount = try { BigDecimal(amountText) } catch (e: Exception) { BigDecimal.ZERO }
    val isAmountValid = selectedPlafond?.let { amount > BigDecimal.ZERO && amount <= it.maxAmount } ?: false
    
    val totalInterest = selectedPlafond?.let {
        amount.multiply(it.interestRate)
            .divide(BigDecimal("100"), 2, RoundingMode.HALF_UP)
            .multiply(BigDecimal(tenor))
    } ?: BigDecimal.ZERO
    
    val totalRepayment = amount.add(totalInterest)
    
    val monthlyInstallment = if (tenor > 0) {
        totalRepayment.divide(BigDecimal(tenor), 0, RoundingMode.CEILING)
    } else {
        BigDecimal.ZERO
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)) // Emerald 50
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header with gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = bgGradient,
                                start = Offset(0f, 0f),
                                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                            )
                        )
                ) {
                    // Decorative pattern
                    Canvas(modifier = Modifier.matchParentSize()) {
                        // Diamond pattern
                        for (i in 0..5) {
                            drawCircle(
                                color = Color.White.copy(alpha = 0.05f),
                                center = Offset(size.width * (0.1f + i * 0.2f), size.height * 0.5f),
                                radius = size.width * 0.15f
                            )
                        }
                    }
                    
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🧮", fontSize = 20.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        "Simulasi Pinjaman",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        "Tanpa perlu login",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }
                            
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f))
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Step Indicator
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StepIndicator(
                                step = 1,
                                label = "Pilih Plafond",
                                isActive = currentStep == 1,
                                isCompleted = currentStep > 1,
                                primaryColor = primaryColor
                            )
                            
                            Box(
                                modifier = Modifier
                                    .width(40.dp)
                                    .height(2.dp)
                                    .background(
                                        if (currentStep > 1) Color.White
                                        else Color.White.copy(alpha = 0.3f)
                                    )
                            )
                            
                            StepIndicator(
                                step = 2,
                                label = "Hitung",
                                isActive = currentStep == 2,
                                isCompleted = false,
                                primaryColor = primaryColor
                            )
                        }
                    }
                }
                
                // Content
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    when (currentStep) {
                        1 -> StandaloneStep1SelectPlafond(
                            plafonds = plafonds,
                            selectedPlafond = selectedPlafond,
                            onSelectPlafond = { selectedPlafond = it },
                            primaryColor = primaryColor
                        )
                        2 -> StandaloneStep2Calculate(
                            plafond = selectedPlafond!!,
                            amountText = amountText,
                            onAmountChange = { 
                                amountText = it.filter { c -> c.isDigit() }
                                showResult = false
                            },
                            tenor = tenor,
                            onTenorChange = { 
                                tenor = it
                                showResult = false
                            },
                            isAmountValid = isAmountValid,
                            showResult = showResult,
                            onCalculate = { showResult = true },
                            amount = amount,
                            totalInterest = totalInterest,
                            totalRepayment = totalRepayment,
                            monthlyInstallment = monthlyInstallment,
                            primaryColor = primaryColor,
                            secondaryColor = secondaryColor
                        )
                    }
                }
                
                // Bottom Navigation
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (currentStep > 1) {
                            OutlinedButton(
                                onClick = { currentStep-- },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = primaryColor
                                ),
                                border = BorderStroke(1.dp, primaryColor)
                            ) {
                                Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Kembali", fontWeight = FontWeight.SemiBold)
                            }
                        }
                        
                        Button(
                            onClick = {
                                if (currentStep == 1 && selectedPlafond != null) {
                                    currentStep = 2
                                } else if (currentStep == 2) {
                                    onDismiss()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = when (currentStep) {
                                1 -> selectedPlafond != null
                                2 -> true
                                else -> false
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryColor,
                                disabledContainerColor = Color.LightGray
                            )
                        ) {
                            Text(
                                when (currentStep) {
                                    1 -> "Lanjutkan"
                                    else -> "Selesai"
                                },
                                fontWeight = FontWeight.Bold
                            )
                            if (currentStep == 1) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepIndicator(
    step: Int,
    label: String,
    isActive: Boolean,
    isCompleted: Boolean,
    primaryColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isCompleted -> Color.White
                        isActive -> Color.White
                        else -> Color.White.copy(alpha = 0.3f)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Text(
                    "$step",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) primaryColor else Color.Gray
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            label,
            fontSize = 9.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive || isCompleted) Color.White else Color.White.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun StandaloneStep1SelectPlafond(
    plafonds: List<Plafond>,
    selectedPlafond: Plafond?,
    onSelectPlafond: (Plafond) -> Unit,
    primaryColor: Color
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Pilih Jenis Plafond",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF064E3B)
            )
            Text(
                "Pilih plafond yang sesuai dengan kebutuhan Anda",
                fontSize = 12.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        items(plafonds) { plafond ->
            val isSelected = selectedPlafond?.id == plafond.id
            val plafondColor = getPlafondColor(plafond.name)
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectPlafond(plafond) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) primaryColor.copy(alpha = 0.1f) else Color.White
                ),
                border = if (isSelected) BorderStroke(2.dp, primaryColor) else null,
                elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Plafond color indicator
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(plafondColor, plafondColor.copy(alpha = 0.7f))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            plafond.name.first().toString(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            plafond.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black.copy(alpha = 0.85f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PlafondInfoChip(
                                label = "Max ${formatCurrency(plafond.maxAmount)}",
                                color = plafondColor
                            )
                            PlafondInfoChip(
                                label = "${plafond.interestRate}%/bln",
                                color = Color(0xFFFF9800)
                            )
                        }
                    }
                    
                    // Selection indicator
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) primaryColor else Color.LightGray.copy(alpha = 0.3f)
                            )
                            .border(
                                width = if (isSelected) 0.dp else 2.dp,
                                color = if (isSelected) Color.Transparent else Color.LightGray,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
        
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun PlafondInfoChip(label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

@Composable
private fun StandaloneStep2Calculate(
    plafond: Plafond,
    amountText: String,
    onAmountChange: (String) -> Unit,
    tenor: Int,
    onTenorChange: (Int) -> Unit,
    isAmountValid: Boolean,
    showResult: Boolean,
    onCalculate: () -> Unit,
    amount: BigDecimal,
    totalInterest: BigDecimal,
    totalRepayment: BigDecimal,
    monthlyInstallment: BigDecimal,
    primaryColor: Color,
    secondaryColor: Color
) {
    val plafondColor = getPlafondColor(plafond.name)
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Selected Plafond Info
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = plafondColor.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, plafondColor.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(plafondColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            plafond.name.first().toString(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            plafond.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = plafondColor
                        )
                        Text(
                            "Bunga ${plafond.interestRate}%/bulan • Tenor ${plafond.tenorMin}-${plafond.tenorMax} bulan",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
        
        // Amount Input
        item {
            Column {
                Text(
                    "Jumlah Pinjaman",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = onAmountChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Masukkan jumlah") },
                    prefix = { Text("Rp ", fontWeight = FontWeight.Bold, color = primaryColor) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = Color.LightGray,
                        cursorColor = primaryColor
                    ),
                    singleLine = true,
                    isError = amountText.isNotEmpty() && !isAmountValid,
                    supportingText = if (amountText.isNotEmpty() && !isAmountValid) {
                        { Text("Maksimal ${formatCurrency(plafond.maxAmount)}", color = MaterialTheme.colorScheme.error) }
                    } else null
                )
                
                // Quick amount chips
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("5jt" to "5000000", "10jt" to "10000000", "25jt" to "25000000", "50jt" to "50000000").forEach { (label, value) ->
                        val isDisabled = BigDecimal(value) > plafond.maxAmount
                        val isSelected = amountText == value
                        FilterChip(
                            selected = isSelected,
                            onClick = { if (!isDisabled) onAmountChange(value) },
                            label = { Text(label, fontSize = 10.sp) },
                            modifier = Modifier.weight(1f),
                            enabled = !isDisabled,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = primaryColor.copy(alpha = 0.2f),
                                selectedLabelColor = primaryColor
                            )
                        )
                    }
                }
            }
        }
        
        // Tenor Input
        item {
            Column {
                Text(
                    "Tenor (Bulan)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                // Tenor stepper
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledIconButton(
                        onClick = { if (tenor > plafond.tenorMin) onTenorChange(tenor - 1) },
                        enabled = tenor > plafond.tenorMin,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = primaryColor,
                            disabledContainerColor = Color.LightGray
                        )
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Kurangi")
                    }
                    
                    Spacer(modifier = Modifier.width(24.dp))
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "$tenor",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                        Text(
                            "Bulan",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(24.dp))
                    
                    FilledIconButton(
                        onClick = { if (tenor < plafond.tenorMax) onTenorChange(tenor + 1) },
                        enabled = tenor < plafond.tenorMax,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = primaryColor,
                            disabledContainerColor = Color.LightGray
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Tambah")
                    }
                }
                
                // Slider
                Slider(
                    value = tenor.toFloat(),
                    onValueChange = { onTenorChange(it.toInt()) },
                    valueRange = plafond.tenorMin.toFloat()..plafond.tenorMax.toFloat(),
                    steps = plafond.tenorMax - plafond.tenorMin - 1,
                    colors = SliderDefaults.colors(
                        thumbColor = primaryColor,
                        activeTrackColor = primaryColor,
                        inactiveTrackColor = primaryColor.copy(alpha = 0.2f)
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${plafond.tenorMin} bulan", fontSize = 10.sp, color = Color.Gray)
                    Text("${plafond.tenorMax} bulan", fontSize = 10.sp, color = Color.Gray)
                }
            }
        }
        
        // Calculate Button
        item {
            Button(
                onClick = onCalculate,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = isAmountValid,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = secondaryColor,
                    disabledContainerColor = Color.LightGray
                )
            ) {
                Icon(Icons.Default.Calculate, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Hitung Simulasi", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
        
        // Result (Animated)
        item {
            AnimatedVisibility(
                visible = showResult && isAmountValid,
                enter = fadeIn(animationSpec = tween(300)) + slideInVertically(animationSpec = tween(300)),
                exit = fadeOut() + slideOutVertically()
            ) {
                StandaloneResultCard(
                    plafond = plafond,
                    amount = amount,
                    tenor = tenor,
                    totalInterest = totalInterest,
                    totalRepayment = totalRepayment,
                    monthlyInstallment = monthlyInstallment,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor
                )
            }
        }
        
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun StandaloneResultCard(
    plafond: Plafond,
    amount: BigDecimal,
    tenor: Int,
    totalInterest: BigDecimal,
    totalRepayment: BigDecimal,
    monthlyInstallment: BigDecimal,
    primaryColor: Color,
    secondaryColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(primaryColor, secondaryColor)
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
                        Text("📊", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Hasil Simulasi",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Text(
                            plafond.name,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
            
            // Content
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StandaloneResultRow(emoji = "💰", label = "Pinjaman Pokok", value = formatCurrency(amount))
                StandaloneResultRow(emoji = "📅", label = "Tenor", value = "$tenor Bulan")
                StandaloneResultRow(emoji = "📈", label = "Bunga per Bulan", value = "${plafond.interestRate}%")
                
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                
                StandaloneResultRow(
                    emoji = "💸",
                    label = "Total Bunga",
                    value = formatCurrency(totalInterest),
                    valueColor = Color(0xFFFF9800)
                )
                StandaloneResultRow(
                    emoji = "💳",
                    label = "Total Pelunasan",
                    value = formatCurrency(totalRepayment),
                    valueColor = primaryColor,
                    isBold = true
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Monthly highlight
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = secondaryColor
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Cicilan per Bulan",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                            Text(
                                "Estimasi",
                                fontSize = 9.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                        Text(
                            formatCurrency(monthlyInstallment),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
                
                // Disclaimer
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFFBEB) // Amber 50
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("ℹ️", fontSize = 10.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Hasil simulasi ini bersifat estimasi. Untuk mengajukan pinjaman, silakan login terlebih dahulu.",
                            fontSize = 9.sp,
                            color = Color(0xFF92400E),
                            lineHeight = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StandaloneResultRow(
    emoji: String,
    label: String,
    value: String,
    valueColor: Color = Color.Black,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                label,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
        Text(
            value,
            fontSize = if (isBold) 14.sp else 12.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
            color = valueColor
        )
    }
}

/**
 * Footer Section - App info and trust badges
 */
@Composable
fun FooterSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 8.dp)
    ) {
        // Trust badges
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FF)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Dipercaya oleh ribuan pengguna",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TrustBadge(icon = "\ud83d\udee1\ufe0f", label = "Aman")
                    TrustBadge(icon = "\u2705", label = "Terdaftar OJK")
                    TrustBadge(icon = "\ud83d\udd12", label = "Terenkripsi")
                    TrustBadge(icon = "\ud83d\udcde", label = "Support 24/7")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // App info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "\u00a9 2026 Loanova",
                fontSize = 10.sp,
                color = Color.Gray
            )
            Text(
                "  \u2022  ",
                fontSize = 10.sp,
                color = Color.LightGray
            )
            Text(
                "v1.0.0",
                fontSize = 10.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun TrustBadge(icon: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(icon, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            label,
            fontSize = 9.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun RestrictedActionDialog(
    onDismiss: () -> Unit,
    onCompleteProfile: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Profil Belum Lengkap",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Text(
                text = "Kamu harus melengkapi data diri (User Profile) terlebih dahulu sebelum bisa menggunakan fitur ini. Ini penting untuk proses verifikasi data kamu.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray
            )
        },
        confirmButton = {
            Button(
                onClick = onCompleteProfile,
                colors = ButtonDefaults.buttonColors(containerColor = LoanovaBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Lengkapi Sekarang", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Nanti Saja", color = Color.Gray)
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White
    )
}

/**
 * Loan Simulation Dialog - Full screen dialog untuk simulasi pinjaman
 * Design: Modern card-based dengan animasi
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanSimulationDialog(
    plafond: Plafond,
    onDismiss: () -> Unit
) {
    var amountText by remember { mutableStateOf("5000000") }
    var tenor by remember { mutableIntStateOf(plafond.tenorMin) }
    var showResult by remember { mutableStateOf(false) }
    
    val themeColor = getPlafondColor(plafond.name)
    
    // Calculations
    val amount = try { BigDecimal(amountText) } catch (e: Exception) { BigDecimal.ZERO }
    val isAmountValid = amount > BigDecimal.ZERO && amount <= plafond.maxAmount
    
    // Flat Interest Calculation
    val totalInterest = amount
        .multiply(plafond.interestRate)
        .divide(BigDecimal("100"), 2, RoundingMode.HALF_UP)
        .multiply(BigDecimal(tenor))
    
    val totalRepayment = amount.add(totalInterest)
    
    val monthlyInstallment = if (tenor > 0) {
        totalRepayment.divide(BigDecimal(tenor), 0, RoundingMode.CEILING)
    } else {
        BigDecimal.ZERO
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .clickable(enabled = false) {}, // Prevent dismiss on card click
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header dengan gradient plafond
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(themeColor, themeColor.copy(alpha = 0.8f))
                                )
                            )
                    ) {
                        // Decorative elements
                        Canvas(modifier = Modifier.matchParentSize()) {
                            drawCircle(
                                color = Color.White.copy(alpha = 0.1f),
                                center = androidx.compose.ui.geometry.Offset(size.width * 0.9f, size.height * 0.2f),
                                radius = size.width * 0.3f
                            )
                            drawCircle(
                                color = Color.White.copy(alpha = 0.05f),
                                center = androidx.compose.ui.geometry.Offset(size.width * 0.1f, size.height * 0.8f),
                                radius = size.width * 0.2f
                            )
                        }
                        
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            // Handle bar
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .width(40.dp)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color.White.copy(alpha = 0.5f))
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Calculate,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "Simulasi Pinjaman",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = Color.White
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Hitung estimasi cicilan Anda",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                                
                                // Plafond badge
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White.copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                                ) {
                                    Text(
                                        plafond.name,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Info cards row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                SimulationHeaderChip(
                                    modifier = Modifier.weight(1f),
                                    icon = "💰",
                                    label = "Max",
                                    value = formatCurrency(plafond.maxAmount)
                                )
                                SimulationHeaderChip(
                                    modifier = Modifier.weight(1f),
                                    icon = "📊",
                                    label = "Bunga",
                                    value = "${plafond.interestRate}%/bln"
                                )
                                SimulationHeaderChip(
                                    modifier = Modifier.weight(1f),
                                    icon = "📅",
                                    label = "Tenor",
                                    value = "${plafond.tenorMin}-${plafond.tenorMax} bln"
                                )
                            }
                        }
                    }
                    
                    // Content
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Input Amount
                        item {
                            SimulationInputCard(
                                title = "Jumlah Pinjaman",
                                icon = Icons.Default.Payments,
                                themeColor = themeColor
                            ) {
                                OutlinedTextField(
                                    value = amountText,
                                    onValueChange = { 
                                        amountText = it.filter { c -> c.isDigit() }
                                        showResult = false
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("Masukkan jumlah", fontSize = 14.sp) },
                                    prefix = { Text("Rp ", fontWeight = FontWeight.Bold, color = themeColor) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = themeColor,
                                        unfocusedBorderColor = Color.LightGray
                                    ),
                                    singleLine = true,
                                    isError = amountText.isNotEmpty() && !isAmountValid,
                                    supportingText = if (amountText.isNotEmpty() && !isAmountValid) {
                                        { Text("Maksimal ${formatCurrency(plafond.maxAmount)}", color = MaterialTheme.colorScheme.error, fontSize = 11.sp) }
                                    } else null
                                )
                                
                                // Quick amount buttons
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("5jt" to "5000000", "10jt" to "10000000", "25jt" to "25000000", "50jt" to "50000000").forEach { (label, value) ->
                                        val isDisabled = BigDecimal(value) > plafond.maxAmount
                                        AssistChip(
                                            onClick = { 
                                                if (!isDisabled) {
                                                    amountText = value
                                                    showResult = false
                                                }
                                            },
                                            label = { Text(label, fontSize = 10.sp) },
                                            modifier = Modifier.weight(1f),
                                            enabled = !isDisabled,
                                            colors = AssistChipDefaults.assistChipColors(
                                                containerColor = if (amountText == value) themeColor.copy(alpha = 0.1f) else Color.Transparent,
                                                labelColor = if (isDisabled) Color.LightGray else themeColor
                                            ),
                                            border = AssistChipDefaults.assistChipBorder(
                                                enabled = !isDisabled,
                                                borderColor = if (amountText == value) themeColor else Color.LightGray
                                            )
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Input Tenor
                        item {
                            SimulationInputCard(
                                title = "Tenor (Bulan)",
                                icon = Icons.Default.DateRange,
                                themeColor = themeColor
                            ) {
                                // Tenor display
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { 
                                            if (tenor > plafond.tenorMin) {
                                                tenor--
                                                showResult = false
                                            }
                                        },
                                        enabled = tenor > plafond.tenorMin
                                    ) {
                                        Icon(
                                            Icons.Default.RemoveCircle,
                                            contentDescription = "Kurangi",
                                            tint = if (tenor > plafond.tenorMin) themeColor else Color.LightGray,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.width(16.dp))
                                    
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(themeColor.copy(alpha = 0.1f))
                                            .border(BorderStroke(1.dp, themeColor.copy(alpha = 0.3f)), RoundedCornerShape(12.dp))
                                            .padding(horizontal = 24.dp, vertical = 12.dp)
                                    ) {
                                        Text(
                                            "$tenor Bulan",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = themeColor
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.width(16.dp))
                                    
                                    IconButton(
                                        onClick = { 
                                            if (tenor < plafond.tenorMax) {
                                                tenor++
                                                showResult = false
                                            }
                                        },
                                        enabled = tenor < plafond.tenorMax
                                    ) {
                                        Icon(
                                            Icons.Default.AddCircle,
                                            contentDescription = "Tambah",
                                            tint = if (tenor < plafond.tenorMax) themeColor else Color.LightGray,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                                
                                // Tenor slider
                                Spacer(modifier = Modifier.height(8.dp))
                                Slider(
                                    value = tenor.toFloat(),
                                    onValueChange = { 
                                        tenor = it.toInt()
                                        showResult = false
                                    },
                                    valueRange = plafond.tenorMin.toFloat()..plafond.tenorMax.toFloat(),
                                    steps = plafond.tenorMax - plafond.tenorMin - 1,
                                    colors = SliderDefaults.colors(
                                        thumbColor = themeColor,
                                        activeTrackColor = themeColor
                                    )
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("${plafond.tenorMin} bln", fontSize = 10.sp, color = Color.Gray)
                                    Text("${plafond.tenorMax} bln", fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                        }
                        
                        // Calculate Button
                        item {
                            Button(
                                onClick = { showResult = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                enabled = isAmountValid,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = themeColor,
                                    disabledContainerColor = Color.LightGray
                                )
                            ) {
                                Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Hitung Simulasi", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                        
                        // Result Card (Animated)
                        item {
                            AnimatedVisibility(
                                visible = showResult && isAmountValid,
                                enter = fadeIn(animationSpec = tween(300)) + slideInVertically(animationSpec = tween(300)),
                                exit = fadeOut() + slideOutVertically()
                            ) {
                                SimulationResultCard(
                                    amount = amount,
                                    tenor = tenor,
                                    interestRate = plafond.interestRate,
                                    totalInterest = totalInterest,
                                    totalRepayment = totalRepayment,
                                    monthlyInstallment = monthlyInstallment,
                                    themeColor = themeColor,
                                    plafondName = plafond.name
                                )
                            }
                        }
                        
                        item { Spacer(modifier = Modifier.height(20.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SimulationHeaderChip(
    modifier: Modifier = Modifier,
    icon: String,
    label: String,
    value: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = Color.White.copy(alpha = 0.15f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, fontSize = 12.sp)
            Text(label, fontSize = 8.sp, color = Color.White.copy(alpha = 0.7f))
            Text(value, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1)
        }
    }
}

@Composable
private fun SimulationInputCard(
    title: String,
    icon: ImageVector,
    themeColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(themeColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = themeColor, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Black.copy(alpha = 0.8f))
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SimulationResultCard(
    amount: BigDecimal,
    tenor: Int,
    interestRate: BigDecimal,
    totalInterest: BigDecimal,
    totalRepayment: BigDecimal,
    monthlyInstallment: BigDecimal,
    themeColor: Color,
    plafondName: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF1A237E),
                                Color(0xFF3949AB)
                            )
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
                            Icons.Default.Receipt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Hasil Simulasi",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Text(
                            plafondName,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
            
            // Content with timeline style
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // Timeline items
                SimulationResultRow(
                    icon = "💵",
                    label = "Pinjaman Pokok",
                    value = formatCurrency(amount),
                    isFirst = true
                )
                SimulationResultRow(
                    icon = "📅",
                    label = "Tenor",
                    value = "$tenor Bulan"
                )
                SimulationResultRow(
                    icon = "📊",
                    label = "Bunga per Bulan",
                    value = "${interestRate}%"
                )
                SimulationResultRow(
                    icon = "📈",
                    label = "Total Bunga ($tenor bulan)",
                    value = formatCurrency(totalInterest),
                    valueColor = Color(0xFFFF9800)
                )
                SimulationResultRow(
                    icon = "💳",
                    label = "Total Pelunasan",
                    value = formatCurrency(totalRepayment),
                    valueColor = Color(0xFF1A237E),
                    isLast = true
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Highlight: Cicilan per bulan
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF4CAF50),
                                    Color(0xFF81C784)
                                )
                            )
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Estimasi Cicilan",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                            Text(
                                "per bulan",
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                        Text(
                            formatCurrency(monthlyInstallment),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Disclaimer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFFF3E0))
                        .padding(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("⚠️", fontSize = 10.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Simulasi ini hanya estimasi. Nilai aktual dapat berbeda berdasarkan kebijakan dan persetujuan kredit.",
                        fontSize = 9.sp,
                        color = Color(0xFF6D4C41),
                        lineHeight = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SimulationResultRow(
    icon: String,
    label: String,
    value: String,
    valueColor: Color = Color.Black,
    isFirst: Boolean = false,
    isLast: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Timeline dot and line
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            if (!isFirst) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(8.dp)
                        .background(Color.LightGray.copy(alpha = 0.5f))
                )
            }
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 12.sp)
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(8.dp)
                        .background(Color.LightGray.copy(alpha = 0.5f))
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Content
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        }
    }
}
