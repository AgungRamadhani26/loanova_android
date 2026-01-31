package com.example.loanova_android.ui.features.plafon

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.loanova_android.data.model.dto.UserPlafondResponse
import com.example.loanova_android.ui.features.home.formatCurrency
import com.example.loanova_android.ui.features.home.getPlafondColor
import com.example.loanova_android.ui.theme.LoanovaBlue
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import com.example.loanova_android.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivePlafondScreen(
    onNavigateBack: () -> Unit,
    viewModel: UserPlafondViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Plafond Aktif", fontWeight = FontWeight.Bold, color = LoanovaBlue) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = LoanovaBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is UserPlafondUiState.Loading -> CircularProgressIndicator(color = LoanovaBlue)
                is UserPlafondUiState.Error -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Terjadi Kesalahan", color = Color.Red, fontWeight = FontWeight.Bold)
                        Text(state.message, color = Color.Gray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.fetchActivePlafond() }, colors = ButtonDefaults.buttonColors(containerColor = LoanovaBlue)) {
                            Text("Coba Lagi")
                        }
                    }
                }
                is UserPlafondUiState.Success -> {
                    ActivePlafondContent(data = state.data)
                }
            }
        }
    }
}

@Composable
fun ActivePlafondContent(data: UserPlafondResponse) {
    val themeColor = getPlafondColor(data.plafondName)
    // Gradient from Theme Color to a cooler Blue (LoanovaBlue) or lighter shade
    // User requested "to white or to blue". Let's try ThemeColor -> LoanovaBlue for a rich look.
    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            themeColor,
            LoanovaBlue
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // SINGLE CARD DESIGN
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp), // Increased height for more info
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(gradientBrush)
            ) {
                // Background Decoration (Circles)
                // Adding subtle circles for "keren" UI
                Canvas(modifier = Modifier.fillMaxSize()) {
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
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // TOP ROW: Logo & Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Loanova Logo
                         Icon(
                            painter = painterResource(id = R.drawable.logo_nova),
                            contentDescription = "Loanova Logo",
                            tint = Color.Unspecified, 
                            modifier = Modifier.size(40.dp)
                        )
                        
                        // Status Pill
                        Surface(
                            color = if (data.isActive) Color(0xFFE8F5E9).copy(alpha = 0.9f) else Color(0xFFFFEBEE).copy(alpha = 0.9f),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.wrapContentSize()
                        ) {
                            Text(
                                text = if (data.isActive) "ACTIVE" else "INACTIVE",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (data.isActive) Color(0xFF1B5E20) else Color(0xFFC62828),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // MIDDLE SECTION: Remaining & Name
                    Column {
                         Text(
                            text = "Remaining Amount",
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.labelMedium,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = formatCurrency(data.remainingAmount),
                            color = Color.White,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                         Text(
                            text = data.plafondName, 
                            color = Color.White.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }

                    // BOTTOM ROW: Max Limit & Date
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // Max Limit
                        Column {
                            Text(
                                text = "Max Limit",
                                color = Color.White.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                text = formatCurrency(data.maxAmount),
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Assigned Date
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Assigned At",
                                color = Color.White.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                text = formatDate(data.assignedAt),
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

fun formatDate(dateString: String): String {
    return try {
        // Handling potentially ISO format with nanos
         val parsed = LocalDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME)
         parsed.format(DateTimeFormatter.ofPattern("MM/yy")) // Credit card expiry style
         // User requested "Assigned At", usually implies a full date, but CC style is MM/YY.
         // Let's use a nice short date: "dd MMM yyyy"
         parsed.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
    } catch (e: Exception) {
        dateString
    }
}
