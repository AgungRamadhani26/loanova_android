package com.example.loanova_android.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.loanova_android.ui.features.auth.login.LoginScreen
import com.example.loanova_android.ui.features.home.HomeScreen
import com.example.loanova_android.ui.features.home.DashboardScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route)
                }
            )
        }
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToDashboard = { username ->
                    navController.navigate(Screen.Dashboard.createRoute(username)) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = Screen.Dashboard.route,
            arguments = listOf(navArgument("username") { type = NavType.StringType })
        ) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            DashboardScreen(username = username)
        }
    }
}
