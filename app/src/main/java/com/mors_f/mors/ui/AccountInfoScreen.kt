package com.mors_f.mors.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mors_f.mors.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AccountInfoScreen(onNavigate: (String) -> Unit) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val user = auth.currentUser

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(user?.uid) {
        if (user != null) {
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    firstName = document.getString("firstName") ?: ""
                    lastName = document.getString("lastName") ?: ""
                    username = document.getString("username") ?: ""
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                    Toast.makeText(context, "Failed to load user data", Toast.LENGTH_SHORT).show()
                }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Sidebar(
                selectedScreen = Screen.Settings.route,
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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopBar(
                title = "Account Information",
                onMenuClick = {
                    scope.launch { drawerState.open() }
                }
            )

            Text(
                text = "Account Information",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = DarkNavy,
                modifier = Modifier.align(Alignment.Start)
            )
            Text(
                text = "Edit your profile details and change your password.",
                color = TextGrey,
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.Start).padding(bottom = 30.dp)
            )

            if (isLoading) {
                CircularProgressIndicator(color = PrimaryBlue)
            } else {
                Surface(
                    modifier = Modifier.width(500.dp),
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 1.dp
                ) {
                    Column(modifier = Modifier.padding(30.dp)) {
                        Text("Edit Details", fontWeight = FontWeight.Bold, color = DarkNavy, modifier = Modifier.padding(bottom = 16.dp))

                        MorsTextField(
                            label = "First Name", 
                            placeholder = "Enter your first name", 
                            icon = Icons.Default.Person,
                            value = firstName,
                            onValueChange = { firstName = it }
                        )
                        MorsTextField(
                            label = "Last Name", 
                            placeholder = "Enter your last name", 
                            icon = Icons.Default.Person,
                            value = lastName,
                            onValueChange = { lastName = it }
                        )
                        MorsTextField(
                            label = "Username", 
                            placeholder = "Enter your username", 
                            icon = Icons.Default.Person,
                            value = username,
                            onValueChange = { username = it }
                        )
                        MorsTextField(
                            label = "Email", 
                            placeholder = "Enter your email", 
                            icon = Icons.Default.Email,
                            value = email,
                            onValueChange = { email = it }
                        )
                        
                        MorsTextField(
                            label = "New Password (Optional)", 
                            placeholder = "Enter new password", 
                            icon = Icons.Default.Lock, 
                            isPassword = true,
                            value = password,
                            onValueChange = { password = it }
                        )

                        Spacer(Modifier.height(30.dp))

                        Button(
                            onClick = {
                                if (user != null) {
                                    isSaving = true
                                    val userMap = hashMapOf(
                                        "firstName" to firstName,
                                        "lastName" to lastName,
                                        "username" to username,
                                        "email" to email
                                    )
                                    
                                    db.collection("users").document(user.uid).update(userMap as Map<String, Any>)
                                        .addOnSuccessListener {
                                            if (password.isNotEmpty()) {
                                                user.updatePassword(password)
                                                    .addOnCompleteListener { pwTask ->
                                                        isSaving = false
                                                        if (pwTask.isSuccessful) {
                                                            Toast.makeText(context, "Account updated successfully", Toast.LENGTH_SHORT).show()
                                                        } else {
                                                            Toast.makeText(context, "Profile updated, but password change failed: ${pwTask.exception?.message}", Toast.LENGTH_LONG).show()
                                                        }
                                                    }
                                            } else {
                                                isSaving = false
                                                Toast.makeText(context, "Account updated successfully", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            isSaving = false
                                            Toast.makeText(context, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(8.dp),
                            enabled = !isSaving
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text("Save Changes")
                            }
                        }
                        
                        TextButton(
                            onClick = { onNavigate(Screen.Profile.route) },
                            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)
                        ) {
                            Text("Back to Profile", color = TextGrey, fontSize = 12.sp)
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(20.dp))
            Text("© 2026 MORS. All rights reserved.", color = TextGrey, fontSize = 10.sp)
        }
    }
}
