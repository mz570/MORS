package com.mors_f.mors.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mors_f.mors.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun LogoutScreen(onConfirmLogout: () -> Unit, onCancel: () -> Unit, onNavigate: (String) -> Unit) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Sidebar(
                selectedScreen = Screen.Logout.route,
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
                .padding(40.dp)
        ) {
            TopBar(
                title = "Logout",
                onMenuClick = {
                    scope.launch { drawerState.open() }
                }
            )

            Text(
                text = "Logout",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = DarkNavy
            )
            Text(
                text = "You will be logged out of your account.",
                color = TextGrey,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 40.dp)
            )

            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Surface(
                    modifier = Modifier.width(400.dp),
                    color = Color.White,
                    shape = RoundedCornerShape(24.dp),
                    shadowElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(BackgroundLight, RoundedCornerShape(20.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Logout, 
                                contentDescription = null, 
                                tint = PrimaryBlue, 
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        
                        Spacer(Modifier.height(24.dp))
                        
                        Text(
                            "Ready to log out?",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkNavy
                        )
                        Text(
                            "Logging out will end your current session.",
                            color = TextGrey,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
                        )

                        Button(
                            onClick = onConfirmLogout,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Log Out")
                        }
                        
                        Spacer(Modifier.height(12.dp))
                        
                        OutlinedButton(
                            onClick = onCancel,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
                        ) {
                            Text("Cancel", color = TextGrey)
                        }
                    }
                }
            }
            
            Spacer(Modifier.weight(1f))
            Text("© 2026 MORS. All rights reserved.", color = TextGrey, fontSize = 10.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}
