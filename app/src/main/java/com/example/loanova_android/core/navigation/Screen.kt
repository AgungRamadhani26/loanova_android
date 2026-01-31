package com.example.loanova_android.core.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Login : Screen("login")
    object Register : Screen("register")
    object CompleteProfile : Screen("complete_profile")
    object EditProfile : Screen("edit_profile")
    object ChangePassword : Screen("change_password")
    object ActivePlafond : Screen("active_plafond")
}
