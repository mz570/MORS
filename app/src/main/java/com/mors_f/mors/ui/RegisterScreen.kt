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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mors_f.mors.ui.theme.*

@Composable
fun RegisterScreen(onLoginClick: () -> Unit, onNavigate: (String) -> Unit) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(500.dp)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "MORS",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = DarkNavy
            )
            Text(
                text = "Register",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue,
                modifier = Modifier.offset(y = (-10).dp)
            )
            Text(
                text = "Create your account",
                color = TextGrey,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            MorsTextField(label = "First name", placeholder = "Enter your first name", icon = Icons.Default.Person, value = firstName, onValueChange = { firstName = it })
            MorsTextField(label = "Last name", placeholder = "Enter your last name", icon = Icons.Default.Person, value = lastName, onValueChange = { lastName = it })
            MorsTextField(label = "User name", placeholder = "Choose a username", icon = Icons.Default.Person, value = username, onValueChange = { username = it })
            MorsTextField(label = "Email", placeholder = "Enter your email address", icon = Icons.Default.Email, value = email, onValueChange = { email = it })
            MorsTextField(label = "Phone Number", placeholder = "Enter your phone number", icon = Icons.Default.Phone, value = phoneNumber, onValueChange = { phoneNumber = it })
            MorsTextField(label = "Password", placeholder = "Create a password", icon = Icons.Default.Lock, isPassword = true, value = password, onValueChange = { password = it })
            MorsTextField(label = "Confirm password", placeholder = "Confirm your password", icon = Icons.Default.Lock, isPassword = true, value = confirmPassword, onValueChange = { confirmPassword = it })

            Button(
                onClick = {
                    if (email.isNotEmpty() && password.isNotEmpty() && firstName.isNotEmpty() && lastName.isNotEmpty() && username.isNotEmpty() && phoneNumber.isNotEmpty()) {
                        if (password == confirmPassword) {
                            isLoading = true
                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val userId = auth.currentUser?.uid
                                        val userMap = hashMapOf(
                                            "firstName" to firstName,
                                            "lastName" to lastName,
                                            "username" to username,
                                            "email" to email,
                                            "phoneNumber" to phoneNumber
                                        )
                                        if (userId != null) {
                                            db.collection("users").document(userId)
                                                .set(userMap)
                                                .addOnSuccessListener {
                                                    isLoading = false
                                                    onNavigate(Screen.Home.route)
                                                }
                                                .addOnFailureListener { e ->
                                                    isLoading = false
                                                    Toast.makeText(context, "Error saving data: ${e.message}", Toast.LENGTH_SHORT).show()
                                                }
                                        }
                                    } else {
                                        isLoading = false
                                        Toast.makeText(context, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        } else {
                            Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(top = 10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(8.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.Person, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Create Account")
                }
            }

            TextButton(
                onClick = onLoginClick,
                modifier = Modifier.padding(top = 10.dp)
            ) {
                Text(
                    text = buildAnnotatedString {
                        append("Already have an account? ")
                        withStyle(style = SpanStyle(color = PrimaryBlue, fontWeight = FontWeight.Bold)) {
                            append("Log In")
                        }
                    },
                    color = TextGrey,
                    fontSize = 12.sp
                )
            }
            
            Spacer(Modifier.height(20.dp))
            Text("© 2026 MORS. All rights reserved.", color = TextGrey, fontSize = 10.sp)
        }
    }
}
