package com.example.loanova_android

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.example.loanova_android.core.navigation.AppNavigation
import com.example.loanova_android.ui.features.auth.login.LoginScreen
import com.example.loanova_android.ui.theme.Loanova_androidTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
    }

    companion object {
        private const val PREFS_NAME = "deep_link_prefs"
        private const val KEY_NAVIGATE_TO = "navigate_to"
        private const val KEY_LOAN_APPLICATION_ID = "loan_application_id"
        
        /**
         * Simpan deep link data ke SharedPreferences agar bisa dibaca oleh HomeScreen
         */
        fun savePendingDeepLink(context: Context, navigateTo: String?, loanApplicationId: String?) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().apply {
                if (navigateTo != null) {
                    putString(KEY_NAVIGATE_TO, navigateTo)
                    putString(KEY_LOAN_APPLICATION_ID, loanApplicationId)
                } else {
                    remove(KEY_NAVIGATE_TO)
                    remove(KEY_LOAN_APPLICATION_ID)
                }
                apply()
            }
        }
        
        /**
         * Baca pending deep link dari SharedPreferences
         */
        fun getPendingDeepLink(context: Context): Pair<String?, String?> {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return Pair(
                prefs.getString(KEY_NAVIGATE_TO, null),
                prefs.getString(KEY_LOAN_APPLICATION_ID, null)
            )
        }
        
        /**
         * Clear pending deep link setelah dihandle
         */
        fun clearPendingDeepLink(context: Context) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().apply {
                remove(KEY_NAVIGATE_TO)
                remove(KEY_LOAN_APPLICATION_ID)
                apply()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        // SECURITY: Mencegah Screen Capture / Recording
        // Layar akan menjadi hitam jika user mencoba screenshot atau screen record
        // SECURITY: Mencegah Screen Capture / Recording
        // Layar akan menjadi hitam jika user mencoba screenshot atau screen record
        window.setFlags(
           android.view.WindowManager.LayoutParams.FLAG_SECURE,
           android.view.WindowManager.LayoutParams.FLAG_SECURE
        )
        
        // SECURITY: Root Detection
        // Cek apakah device di-root. Jika ya, tutup aplikasi demi keamanan.
        val rootBeer = com.scottyab.rootbeer.RootBeer(this)
        if (rootBeer.isRooted) {
            // Tampilkan log atau kirim analitik jika perlu
            // Tutup aplikasi
            finishAffinity() 
            kotlin.system.exitProcess(0)
        }

        askNotificationPermission()
        enableEdgeToEdge()
        
        // Check for deep link from notification and save to SharedPreferences
        handleIntent(intent)
        
        setContent {
            Loanova_androidTheme {
                val navController = rememberNavController()
                
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        AppNavigation(navController = navController)
                    }
                }
            }
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }
    
    private fun handleIntent(intent: Intent?) {
        intent?.let {
            val navigateTo = it.getStringExtra("navigateTo")
            val loanApplicationId = it.getStringExtra("loanApplicationId")
            
            if (navigateTo != null) {
                // Simpan ke SharedPreferences untuk dibaca oleh HomeScreen
                savePendingDeepLink(this, navigateTo, loanApplicationId)
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
            ){
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    Loanova_androidTheme {
        LoginScreen(onLoginSuccess = {}, onNavigateToRegister = {})
    }
}
