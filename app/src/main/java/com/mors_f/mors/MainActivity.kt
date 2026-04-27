package com.mors_f.mors

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.mors_f.mors.ui.*
import com.mors_f.mors.ui.theme.MORSTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MORSTheme {
                MorsApp()
            }
        }
    }
}

@Composable
fun MorsApp() {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()
    var currentUser by remember { mutableStateOf(auth.currentUser) }

    // Listen for auth state changes
    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            currentUser = firebaseAuth.currentUser
        }
        auth.addAuthStateListener(listener)
        onDispose {
            auth.removeAuthStateListener(listener)
        }
    }
    
    // Set Feed as the default screen when logged in
    val startDestination = if (currentUser != null) Screen.Feed.route else Screen.Login.route

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(Screen.Register.route) {
            RegisterScreen(
                onLoginClick = { navController.navigate(Screen.Login.route) },
                onNavigate = { route -> navController.navigate(route) }
            )
        }
        composable(Screen.Login.route) {
            LoginScreen(
                onRegisterClick = { navController.navigate(Screen.Register.route) },
                onNavigate = { route -> 
                    navController.navigate(route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Home.route) {
            if (currentUser == null) {
                LaunchedEffect(Unit) { navController.navigate(Screen.Login.route) }
            } else {
                HomeScreen(onNavigate = { route -> navController.navigate(route) })
            }
        }
        composable(Screen.Upload.route) {
            if (currentUser == null) {
                LaunchedEffect(Unit) { navController.navigate(Screen.Login.route) }
            } else {
                UploadScreen(onNavigate = { route -> navController.navigate(route) })
            }
        }
        composable(Screen.Feed.route) {
            if (currentUser == null) {
                LaunchedEffect(Unit) { navController.navigate(Screen.Login.route) }
            } else {
                FeedScreen(onNavigate = { route -> navController.navigate(route) })
            }
        }
        composable(Screen.Profile.route) {
            if (currentUser == null) {
                LaunchedEffect(Unit) { navController.navigate(Screen.Login.route) }
            } else {
                ProfileScreen(onNavigate = { route -> navController.navigate(route) })
            }
        }
        composable(Screen.Settings.route) {
            if (currentUser == null) {
                LaunchedEffect(Unit) { navController.navigate(Screen.Login.route) }
            } else {
                SettingsScreen(onNavigate = { route -> navController.navigate(route) })
            }
        }
        composable(Screen.AccountInfo.route) {
            if (currentUser == null) {
                LaunchedEffect(Unit) { navController.navigate(Screen.Login.route) }
            } else {
                AccountInfoScreen(onNavigate = { route -> navController.navigate(route) })
            }
        }
        composable(Screen.Notifications.route) {
            if (currentUser == null) {
                LaunchedEffect(Unit) { navController.navigate(Screen.Login.route) }
            } else {
                NotificationsScreen(onNavigate = { route -> navController.navigate(route) })
            }
        }
        composable(Screen.Logout.route) {
            LogoutScreen(
                onConfirmLogout = { 
                    auth.signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onCancel = { navController.popBackStack() },
                onNavigate = { route -> navController.navigate(route) }
            )
        }
    }
}
