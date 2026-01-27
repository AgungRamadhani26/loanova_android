package com.example.loanova_android.core.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Login : Screen("login")
    object Register : Screen("register")
    object Dashboard : Screen("dashboard/{username}") {
        fun createRoute(username: String) = "dashboard/$username"
    }
}
