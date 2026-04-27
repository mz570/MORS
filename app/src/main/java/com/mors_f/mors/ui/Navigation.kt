package com.mors_f.mors.ui

sealed class Screen(val route: String) {
    object Register : Screen("register")
    object Login : Screen("login")
    object Home : Screen("home")
    object Upload : Screen("upload")
    object Feed : Screen("feed")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object AccountInfo : Screen("account_info")
    object Notifications : Screen("notifications")
    object Logout : Screen("logout")
}
