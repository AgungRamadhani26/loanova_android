package com.example.loanova_android.ui.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
        topBar = {
            HomeHeader(
                isLoggedIn = uiState.isLoggedIn,
                username = uiState.username,
                onLogin = onNavigateToLogin,
                onLogout = { viewModel.logout() }
            )
        },
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
                onNavigateToLogin = onNavigateToLogin
            )

            3 -> ProfileScreen(
                padding = padding,
                onLogout = { 
                    viewModel.logout() 
                },
                onNavigateToCompleteProfile = onNavigateToCompleteProfile
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
    onNavigateToLogin: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item { HeroSection(onNavigateToLogin, uiState.isLoggedIn) }
        item { FeatureSection() }
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
        
        item { SecuritySection() }
        item { StepsSection() }
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
        windowInsets = WindowInsets(0, 0, 0, 0), // Remove default system bar padding to lower the bar
        modifier = Modifier.height(80.dp)
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Beranda") },
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = LoanovaBlue,
                selectedTextColor = LoanovaBlue,
                indicatorColor = LoanovaBlue.copy(alpha = 0.1f)
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.MonetizationOn, contentDescription = "Pinjaman") },
            label = { Text("Pinjaman") },
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = LoanovaBlue,
                selectedTextColor = LoanovaBlue,
                indicatorColor = LoanovaBlue.copy(alpha = 0.1f)
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Notifications, contentDescription = "Notifikasi") },
            label = { Text("Notifikasi") },
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = LoanovaBlue,
                selectedTextColor = LoanovaBlue,
                indicatorColor = LoanovaBlue.copy(alpha = 0.1f)
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Profil") },
            label = { Text("Profil") },
            selected = selectedTab == 3,
            onClick = { onTabSelected(3) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = LoanovaBlue,
                selectedTextColor = LoanovaBlue,
                indicatorColor = LoanovaBlue.copy(alpha = 0.1f)
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeHeader(
    isLoggedIn: Boolean,
    username: String?,
    onLogin: () -> Unit,
    onLogout: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccountBalance,
                    contentDescription = null,
                    tint = LoanovaBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "LOANOVA",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        color = LoanovaBlue
                    )
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.White
        ),
        actions = {
            if (isLoggedIn) {
                IconButton(onClick = onLogout) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Logout",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            } else {
                IconButton(onClick = onLogin) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Login,
                        contentDescription = "Login",
                        tint = LoanovaBlue
                    )
                }
            }
        }
    )
}

@Composable
fun HeroSection(onNavigateToLogin: () -> Unit, isLoggedIn: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(LoanovaBlue, LoanovaLightBlue)
                )
            )
            .padding(24.dp)
    ) {
        Column {
            Text(
                text = if (isLoggedIn) "Solusi Finansial Anda" else "Ajukan Pinjaman dengan Mudah, Aman, dan Transparan",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 32.sp
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Solusi finansial masa depan dengan teknologi verifikasi tercanggih. Proses 100% online.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White.copy(alpha = 0.8f)
                )
            )
            Spacer(modifier = Modifier.height(24.dp))
            if (!isLoggedIn) {
                Button(
                    onClick = onNavigateToLogin,
                    colors = ButtonDefaults.buttonColors(containerColor = LoanovaGold),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Ajukan Pinjaman Sekarang",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun FeatureSection() {
    val features = listOf(
        FeatureItem("100% Online", "Tanpa tatap muka", Icons.Default.Smartphone),
        FeatureItem("Cepat", "Persetujuan instan", Icons.Default.Bolt),
        FeatureItem("Aman", "Enkripsi tingkat tinggi", Icons.Default.Lock),
        FeatureItem("24/7 Support", "Bantuan kapan saja", Icons.Default.SupportAgent)
    )

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Kenapa Memilih Kami?",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            features.forEach { feature ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(LoanovaBlue.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = feature.icon, contentDescription = null, tint = LoanovaBlue)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = feature.title,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun PlafondTitleSection() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Pilihan Plafon Pinjaman",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Sesuaikan dengan kebutuhan finansial Anda",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Composable
fun PlafondListSection(plafonds: List<Plafond>) {
    if (plafonds.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
            Text("Belum ada data plafond", color = Color.Gray)
        }
        return
    }

    // Grid layout implementation manually (Row of 2 columns)
    val chunked = plafonds.chunked(2)
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        chunked.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { plafond ->
                    PlafondCard(plafond = plafond, modifier = Modifier.weight(1f))
                }
                // Fill empty space if odd number of items
                if (rowItems.size < 2) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun PlafondCard(plafond: Plafond, modifier: Modifier = Modifier) {
    val themeColor = getPlafondColor(plafond.name)
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), // Increased elevation
        border = androidx.compose.foundation.BorderStroke(2.dp, themeColor) // Thicker, opaque border
    ) {
        Column(
            modifier = Modifier
                .background(themeColor.copy(alpha = 0.1f)) // Slightly more visible background tint
                .padding(16.dp)
        ) {
            Text(
                text = plafond.name, 
                fontWeight = FontWeight.Black, // Extra bold
                color = themeColor,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = plafond.description, 
                style = MaterialTheme.typography.bodySmall, 
                color = Color.Black.copy(alpha = 0.7f), // Darker text for readability
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Max Limit", 
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), 
                color = Color.Black.copy(alpha = 0.6f)
            )
            Text(
                text = formatCurrency(plafond.maxAmount), 
                fontWeight = FontWeight.Black, 
                fontSize = 16.sp, // Reduced font size
                color = themeColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Bunga ${plafond.interestRate}% / bulan", 
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), 
                color = themeColor
            )
        }
    }
}

fun getPlafondColor(name: String): Color {
    return when {
        name.contains("Gold", ignoreCase = true) -> Color(0xFFFFB300) // Amber-600 (Brighter Gold)
        name.contains("Silver", ignoreCase = true) -> Color(0xFF64748B) // Slate-500 (Distinct Silver/Grey)
        name.contains("Bronze", ignoreCase = true) -> Color(0xFFD2691E) // Chocolate (Vibrant Bronze)
        name.contains("Platinum", ignoreCase = true) -> Color(0xFF334155) // Slate-700 (Strong Dark Grey)
        name.contains("Red", ignoreCase = true) -> Color(0xFFEF4444) // Red-500 (Bright Red)
        name.contains("Black", ignoreCase = true) -> Color(0xFF000000)
        else -> LoanovaBlue
    }
}

fun formatCurrency(amount: java.math.BigDecimal): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    format.maximumFractionDigits = 0 // Remove decimals
    return format.format(amount).replace("Rp", "Rp ")
}

@Composable
fun SecuritySection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1E293B))
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.VerifiedUser,
                contentDescription = null,
                tint = Color.Cyan,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Aman & Terpercaya",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Diawasi oleh OJK dan menggunakan enkripsi AES-256 bit.",
                    color = Color.LightGray,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun StepsSection() {
    val steps = listOf(
        "Daftar Akun",
        "Lengkapi Profil",
        "Pilih Plafon",
        "Dana Cair"
    )

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "4 Langkah Mudah",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            steps.forEachIndexed { index, title ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(LoanovaBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "${index + 1}", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

data class FeatureItem(val title: String, val desc: String, val icon: ImageVector)

