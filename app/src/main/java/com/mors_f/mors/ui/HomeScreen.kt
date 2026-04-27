package com.mors_f.mors.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mors_f.mors.R
import com.mors_f.mors.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(onNavigate: (String) -> Unit) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUserId = auth.currentUser?.uid
    
    var firstName by remember { mutableStateOf("User") }

    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            db.collection("users").document(currentUserId).get()
                .addOnSuccessListener { document ->
                    firstName = document.getString("firstName") ?: "User"
                }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Sidebar(
                selectedScreen = Screen.Home.route,
                onNavigate = { route ->
                    scope.launch { drawerState.close() }
                    onNavigate(route)
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(horizontal = 24.dp, vertical = 40.dp)
        ) {
            TopBar(
                title = "Home",
                onMenuClick = {
                    scope.launch { drawerState.open() }
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_mors_logo),
                    contentDescription = "MORS Logo",
                    modifier = Modifier.size(48.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Home",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DarkNavy
                )
            }

            Text(
                text = "Hello, $firstName 👋",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = DarkNavy
            )
            Text(
                text = "Welcome! What would you like to do?",
                color = TextGrey,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 30.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                QuickActionCard(
                    title = "Profile",
                    subtitle = "View and update your profile picture",
                    icon = Icons.Default.Portrait,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.Profile.route) }
                )
                QuickActionCard(
                    title = "Feed",
                    subtitle = "View your feed and updates",
                    icon = Icons.AutoMirrored.Filled.List,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.Feed.route) }
                )
                QuickActionCard(
                    title = "Upload",
                    subtitle = "Share your moments with others",
                    icon = Icons.Default.FileUpload,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.Upload.route) }
                )
            }
            
            Spacer(Modifier.height(20.dp))
            
            QuickActionCard(
                title = "Notifications",
                subtitle = "Check your latest likes and comments",
                icon = Icons.Default.Notifications,
                modifier = Modifier.fillMaxWidth(),
                onClick = { onNavigate(Screen.Notifications.route) }
            )

            Spacer(Modifier.weight(1f))
            Text("© 2026 MORS. All rights reserved.", color = TextGrey, fontSize = 10.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

@Composable
fun QuickActionCard(title: String, subtitle: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier
            .height(180.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(BackgroundLight, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkNavy)
            Text(subtitle, color = TextGrey, fontSize = 10.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
        }
    }
}
