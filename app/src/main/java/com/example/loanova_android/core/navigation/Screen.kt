package com.example.loanova_android.core.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object ResetPassword : Screen("reset_password")
    object CompleteProfile : Screen("complete_profile")
    object EditProfile : Screen("edit_profile")
    object ChangePassword : Screen("change_password")
    object ActivePlafond : Screen("active_plafond")
    object LoanApplication : Screen("loan_application")
    object LoanHistory : Screen("loan_history")
}

