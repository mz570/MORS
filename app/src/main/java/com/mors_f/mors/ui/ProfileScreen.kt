package com.mors_f.mors.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mors_f.mors.R
import com.mors_f.mors.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(onNavigate: (String) -> Unit) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid
    
    var userData by remember { mutableStateOf<Map<String, Any>?>(null) }
    var userPosts by remember { mutableStateOf<List<Pair<String, Map<String, Any>>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isUploading by remember { mutableStateOf(false) }

    // Fetch User Data and User Posts
    LaunchedEffect(userId) {
        if (userId != null) {
            // Fetch User Details
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    userData = document.data
                }
            
            // Fetch User's Posts
            db.collection("posts")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { value, error ->
                    if (value != null) {
                        userPosts = value.documents.map { it.id to (it.data ?: emptyMap()) }
                    }
                    isLoading = false
                }
        }
    }

    // Image Picker Launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            isUploading = true
            MediaManager.get().upload(it)
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String?) {}
                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                    override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                        val imageUrl = resultData?.get("secure_url") as? String
                        if (imageUrl != null && userId != null) {
                            db.collection("users").document(userId)
                                .update("profilePicture", imageUrl)
                                .addOnSuccessListener {
                                    isUploading = false
                                    userData = userData?.toMutableMap()?.apply { put("profilePicture", imageUrl) }
                                    Toast.makeText(context, "Profile picture updated", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener {
                                    isUploading = false
                                    Toast.makeText(context, "Failed to save image URL", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                    override fun onError(requestId: String?, error: ErrorInfo?) {
                        isUploading = false
                        Toast.makeText(context, "Upload failed: ${error?.description}", Toast.LENGTH_SHORT).show()
                    }
                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                })
                .dispatch()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Sidebar(
                selectedScreen = Screen.Profile.route,
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
                title = "Profile",
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
                    text = "Profile",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DarkNavy
                )
            }
            
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.White,
                            shape = RoundedCornerShape(24.dp),
                            shadowElevation = 2.dp
                        ) {
                            Column(modifier = Modifier.padding(24.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Profile Photo", 
                                            fontWeight = FontWeight.Bold, 
                                            fontSize = 18.sp,
                                            color = DarkNavy
                                        )
                                        Text(
                                            "This photo will be visible to everyone",
                                            fontSize = 12.sp,
                                            color = TextGrey
                                        )
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .size(90.dp)
                                            .clip(CircleShape)
                                            .background(BackgroundLight),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val profilePicUrl = userData?.get("profilePicture") as? String
                                        if (profilePicUrl != null) {
                                            AsyncImage(
                                                model = profilePicUrl,
                                                contentDescription = "Profile Picture",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Image(
                                                painter = painterResource(id = R.drawable.ic_mors_logo),
                                                contentDescription = null,
                                                modifier = Modifier.size(60.dp),
                                                alpha = 0.5f
                                            )
                                        }
                                        
                                        if (isUploading) {
                                            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
                                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(28.dp))
                                            }
                                        }
                                    }
                                }

                                Spacer(Modifier.height(16.dp))
                                
                                Button(
                                    onClick = { imagePickerLauncher.launch("image/*") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                    enabled = !isUploading
                                ) {
                                    Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Change Profile Photo", fontWeight = FontWeight.Bold)
                                }

                                Spacer(Modifier.height(24.dp))

                                ProfileInfoRow(label = "First Name", value = userData?.get("firstName")?.toString() ?: "N/A")
                                ProfileInfoRow(label = "Last Name", value = userData?.get("lastName")?.toString() ?: "N/A")
                                ProfileInfoRow(label = "Username", value = userData?.get("username")?.toString() ?: "N/A")
                                ProfileInfoRow(label = "Email", value = userData?.get("email")?.toString() ?: "N/A")
                                
                                Spacer(Modifier.height(20.dp))

                                TextButton(
                                    onClick = { onNavigate(Screen.Settings.route) },
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                ) {
                                    Text("Update details in Account Information", color = PrimaryBlue, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Article, contentDescription = null, tint = DarkNavy)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Your Activity",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkNavy
                            )
                        }
                    }

                    if (userPosts.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
                                Text("No posts shared yet.", color = TextGrey, fontSize = 16.sp)
                            }
                        }
                    } else {
                        items(userPosts) { (postId, postData) ->
                            PostCard(postData = postData, postId = postId, canDelete = true)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 10.dp)) {
        Text(text = label, color = TextGrey, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(4.dp))
        Text(text = value, color = DarkNavy, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        HorizontalDivider(color = BackgroundLight.copy(alpha = 0.8f), thickness = 1.dp, modifier = Modifier.padding(top = 10.dp))
    }
}
