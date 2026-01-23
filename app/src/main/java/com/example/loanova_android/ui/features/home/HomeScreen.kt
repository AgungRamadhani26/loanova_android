package com.example.loanova_android.ui.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.loanova_android.ui.theme.*

@Composable
fun HomeScreen(
    onNavigateToLogin: () -> Unit = {}
) {
    Scaffold(
        topBar = { HomeHeader() },
        containerColor = LoanovaBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item { HeroSection(onNavigateToLogin) }
            item { FeatureSection() }
            item { PlafondTitleSection() }
            item { PlafondListSection() }
            item { SecuritySection() }
            item { StepsSection() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeHeader() {
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
            IconButton(onClick = { /* TODO: Profile or Notifications */ }) {
                Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.Gray)
            }
        }
    )
}

@Composable
fun HeroSection(onNavigateToLogin: () -> Unit) {
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
                text = "Ajukan Pinjaman dengan Mudah, Aman, dan Transparan",
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
fun PlafondListSection() {
    val plafonds = listOf(
        PlafondData("Bronze", "Untuk pemula", "Rp 5.000.000", "0.5%"),
        PlafondData("Silver", "Kebutuhan sedang", "Rp 15.000.000", "0.4%"),
        PlafondData("Gold", "Kebutuhan besar", "Rp 50.000.000", "0.3%"),
        PlafondData("Platinum", "Solusi korporat", "Rp 100.000.000", "0.2%")
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        plafonds.take(2).forEach { plafond ->
            PlafondCard(plafond = plafond, modifier = Modifier.weight(1f))
        }
    }
    Spacer(modifier = Modifier.height(12.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        plafonds.drop(2).forEach { plafond ->
            PlafondCard(plafond = plafond, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun PlafondCard(plafond: PlafondData, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = plafond.name, fontWeight = FontWeight.Bold, color = LoanovaBlue)
            Text(text = plafond.desc, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Max Amount", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(text = plafond.amount, fontWeight = FontWeight.Black, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Bunga ${plafond.interest} / bulan", style = MaterialTheme.typography.labelSmall, color = LoanovaBlue)
        }
    }
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
data class PlafondData(val name: String, val desc: String, val amount: String, val interest: String)
