package com.mors_f.mors.ui

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.mors_f.mors.R
import com.mors_f.mors.ui.theme.*

// Web Client ID found in google-services.json (client_type 3)
const val WEB_CLIENT_ID = "44177632933-sof0cq14cu0tfomlj5fa07lhrmjha6fv.apps.googleusercontent.com"

@Composable
fun LoginScreen(onRegisterClick: () -> Unit, onNavigate: (String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    // Google Sign-In Setup
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                isLoading = true
                auth.signInWithCredential(credential)
                    .addOnCompleteListener { authTask ->
                        if (authTask.isSuccessful) {
                            val user = auth.currentUser
                            if (user != null) {
                                // Check if user exists in Firestore for Google Login
                                db.collection("users").document(user.uid).get()
                                    .addOnSuccessListener { document ->
                                        if (!document.exists()) {
                                            // Auto-register Google user if they don't exist
                                            val userMap = hashMapOf(
                                                "firstName" to (user.displayName?.split(" ")?.getOrNull(0) ?: ""),
                                                "lastName" to (user.displayName?.split(" ")?.getOrNull(1) ?: ""),
                                                "username" to (user.email?.split("@")?.get(0) ?: "user"),
                                                "email" to (user.email ?: "")
                                            )
                                            db.collection("users").document(user.uid).set(userMap)
                                                .addOnSuccessListener {
                                                    isLoading = false
                                                    onNavigate(Screen.Home.route)
                                                }
                                        } else {
                                            isLoading = false
                                            onNavigate(Screen.Home.route)
                                        }
                                    }
                            }
                        } else {
                            isLoading = false
                            Toast.makeText(context, "Firebase auth failed: ${authTask.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } catch (e: ApiException) {
                isLoading = false
                Toast.makeText(context, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(400.dp)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "MORS",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = DarkNavy,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            Text(
                text = "Login",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = DarkNavy
            )
            Text(
                text = "Welcome! Please sign in to continue.",
                color = TextGrey,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 30.dp)
            )

            MorsTextField(
                label = "Email",
                placeholder = "Enter your email",
                icon = Icons.Default.Email,
                value = email,
                onValueChange = { email = it }
            )
            MorsTextField(
                label = "Password",
                placeholder = "Enter your password",
                icon = Icons.Default.Lock,
                isPassword = true,
                value = password,
                onValueChange = { password = it }
            )

            Button(
                onClick = {
                    if (email.isNotEmpty() && password.isNotEmpty()) {
                        isLoading = true
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val userId = auth.currentUser?.uid
                                    if (userId != null) {
                                        // CHECK IF REGISTERED IN FIRESTORE
                                        db.collection("users").document(userId).get()
                                            .addOnSuccessListener { document ->
                                                isLoading = false
                                                if (document.exists()) {
                                                    onNavigate(Screen.Home.route)
                                                } else {
                                                    // Not registered in app (no Firestore record)
                                                    auth.signOut()
                                                    Toast.makeText(context, "Account not fully registered. Please use the Register screen.", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                            .addOnFailureListener {
                                                isLoading = false
                                                auth.signOut()
                                                Toast.makeText(context, "Verification failed. Try again.", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                } else {
                                    isLoading = false
                                    Toast.makeText(
                                        context,
                                        "Login failed: ${task.exception?.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    } else {
                        Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .padding(top = 20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(8.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Login")
                }
            }

            Text("OR", modifier = Modifier.padding(vertical = 16.dp), color = TextGrey)

            GoogleLoginButton(
                text = "Continue with Google",
                onClick = {
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(WEB_CLIENT_ID)
                        .requestEmail()
                        .build()
                    val googleSignInClient = GoogleSignIn.getClient(context, gso)
                    googleSignInLauncher.launch(googleSignInClient.signInIntent)
                },
                enabled = !isLoading
            )

            TextButton(
                onClick = onRegisterClick,
                modifier = Modifier.padding(top = 10.dp)
            ) {
                Text(
                    text = buildAnnotatedString {
                        append("Don't have an account? ")
                        withStyle(style = SpanStyle(color = PrimaryBlue, fontWeight = FontWeight.Bold)) {
                            append("Register")
                        }
                    },
                    color = TextGrey,
                    fontSize = 12.sp
                )
            }
            
            Spacer(Modifier.height(40.dp))
            Text("© 2026 MORS. All rights reserved.", color = TextGrey, fontSize = 10.sp)
        }
    }
}

@Composable
fun GoogleLoginButton(text: String, onClick: () -> Unit, enabled: Boolean = true) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextDark),
        enabled = enabled
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_google_logo),
            contentDescription = "Google Logo",
            modifier = Modifier.size(20.dp),
            tint = Color.Unspecified
        )
        Spacer(Modifier.width(12.dp))
        Text(text)
    }
}
