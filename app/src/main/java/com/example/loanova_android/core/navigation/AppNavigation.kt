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
                },
                onNavigateToCompleteProfile = {
                    navController.navigate(Screen.CompleteProfile.route)
                },
                onNavigateToEditProfile = {
                    navController.navigate(Screen.EditProfile.route)
                },
                onNavigateToChangePassword = {
                    navController.navigate(Screen.ChangePassword.route)
                },
                onNavigateToActivePlafond = {
                    navController.navigate(Screen.ActivePlafond.route)
                },
                onNavigateToLoanApplication = {
                    navController.navigate(Screen.LoanApplication.route)
                }
            )
        }
        
        // ====================================================================
        // COMPLETE PROFILE SCREEN
        // Route: "complete_profile"
        // ====================================================================
        composable(Screen.CompleteProfile.route) {
            com.example.loanova_android.ui.features.profile.CompleteProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSuccess = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.EditProfile.route) {
            com.example.loanova_android.ui.features.profile.edit.EditProfileScreen(
                onNavigateUp = {
                    navController.popBackStack()
                }
            )
        }
        
        // ====================================================================
        // REGISTER SCREEN
        // Route: "register"
        // ====================================================================
        composable(Screen.Register.route) {
            com.example.loanova_android.ui.features.auth.register.RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
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
                onLoginSuccess = {
                    // Navigate kembali ke Home
                    // popUpTo(Screen.Home.route) { inclusive = true } 
                    // Artinya: Hapus Home lama (state belum login) dan replace dengan Home baru (state login)
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.ChangePassword.route) {
            com.example.loanova_android.ui.features.profile.password.ChangePasswordScreen(
                onNavigateUp = {
                    navController.popBackStack()
                },
                onNavigateToLogin = {
                    // Logout success -> Go to Login and clear backstack
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true } // Clear everything
                    }
                }
            )
        }

        composable(Screen.ActivePlafond.route) {
            com.example.loanova_android.ui.features.plafon.ActivePlafondScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // ====================================================================
        // LOAN APPLICATION SCREEN
        // Route: "loan_application"
        // ====================================================================
        composable(Screen.LoanApplication.route) {
            com.example.loanova_android.ui.features.loan.LoanApplicationScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSuccess = {
                    // Navigate back to Home after successful submission
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
    }
}

