package com.example.loanova_android.core.navigation

// ============================================================================
// LAYER: Core (Cross-cutting concern)
// PATTERN: Jetpack Compose Navigation
// RESPONSIBILITY: Centralized navigation logic untuk seluruh aplikasi
// ============================================================================

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.loanova_android.ui.features.auth.login.LoginScreen
import com.example.loanova_android.ui.features.home.HomeScreen
import com.example.loanova_android.ui.features.home.DashboardScreen

/**
 * AppNavigation - Composable untuk setup navigation graph aplikasi.
 * 
 * APA ITU NAVIGATION GRAPH?
 * - Navigation Graph adalah peta semua screen dalam aplikasi
 * - Mendefinisikan routes (path ke setiap screen)
 * - Mendefinisikan navigasi (bagaimana berpindah antar screen)
 * - Jetpack Compose Navigation mirip dengan XML-based Navigation Component
 * 
 * KENAPA CENTRALIZED NAVIGATION?
 * 1. SINGLE SOURCE OF TRUTH: Semua routes didefinisikan di satu tempat
 * 2. TYPE-SAFETY: Menggunakan sealed class Screen untuk route definitions
 * 3. MAINTAINABILITY: Mudah menambah/modify screen
 * 4. TESTABILITY: Navigation logic bisa di-test
 * 
 * @param navController Controller untuk navigasi, dibuat di MainActivity
 */
@Composable
fun AppNavigation(navController: NavHostController) {
    /**
     * NavHost - Container untuk navigation graph.
     * 
     * @param navController Controller untuk navigasi
     * @param startDestination Screen pertama yang ditampilkan saat app dibuka
     * 
     * FLOW NAVIGATION di app ini:
     * Home (Landing) → Login → Dashboard
     *                    ↓
     *              (after login success)
     */
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route // App dimulai dari Home/Landing page
    ) {
        // ====================================================================
        // HOME SCREEN (Landing Page)
        // Route: "home"
        // ====================================================================
        composable(Screen.Home.route) {
            HomeScreen(
                // Callback untuk navigasi ke Login
                // Lambda ini akan dipanggil ketika user klik tombol login
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route)
                }
            )
        }
        
        // ====================================================================
        // LOGIN SCREEN
        // Route: "login"
        // ====================================================================
        composable(Screen.Login.route) {
            LoginScreen(
                // Callback dipanggil setelah login sukses
                // Menerima username untuk ditampilkan di Dashboard
                onNavigateToDashboard = { username ->
                    // Navigate ke Dashboard dengan username sebagai argument
                    navController.navigate(Screen.Dashboard.createRoute(username)) {
                        // popUpTo: Hapus screen dari back stack
                        // inclusive = true: Home juga dihapus dari stack
                        // Efek: User tidak bisa back ke Login/Home setelah login
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
        
        // ====================================================================
        // DASHBOARD SCREEN
        // Route: "dashboard/{username}"
        // Menerima argument username dari navigation
        // ====================================================================
        composable(
            route = Screen.Dashboard.route, // "dashboard/{username}"
            // Definisi arguments yang diterima screen ini
            arguments = listOf(
                navArgument("username") { 
                    type = NavType.StringType // Tipe data argument
                }
            )
        ) { backStackEntry ->
            // Extract argument dari back stack entry
            // arguments?.getString("key") untuk mengambil String argument
            val username = backStackEntry.arguments?.getString("username") ?: ""
            
            // Pass username ke DashboardScreen
            DashboardScreen(username = username)
        }
    }
}
