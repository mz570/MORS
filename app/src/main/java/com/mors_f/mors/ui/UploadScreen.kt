package com.mors_f.mors.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mors_f.mors.R
import com.mors_f.mors.ui.theme.*
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun UploadScreen(onNavigate: (String) -> Unit) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var caption by remember { mutableStateOf("") }
    var locationName by remember { mutableStateOf("") }
    var latitude by remember { mutableDoubleStateOf(0.0) }
    var longitude by remember { mutableDoubleStateOf(0.0) }
    var isPosting by remember { mutableStateOf(false) }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            try {
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener { loc ->
                        if (loc != null) {
                            latitude = loc.latitude
                            longitude = loc.longitude
                            locationName = "Location Tagged"
                        } else {
                            Toast.makeText(context, "Cannot get location. Make sure GPS is on.", Toast.LENGTH_SHORT).show()
                        }
                    }
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        } else {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Sidebar(
                selectedScreen = Screen.Upload.route,
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
                .verticalScroll(rememberScrollState())
        ) {
            TopBar(
                title = "Upload",
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
                    text = "Upload",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DarkNavy
                )
            }
            
            Text(
                text = "Share your moments with the world",
                color = TextGrey,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            Spacer(Modifier.height(24.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Upload Picture", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DarkNavy)
                    Spacer(Modifier.height(16.dp))
                    
                    // Drop area / Image Preview
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .border(1.dp, Color.LightGray, RoundedCornerShape(16.dp))
                            .background(BackgroundLight.copy(alpha = 0.5f))
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImageUri != null) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Selected Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                            IconButton(
                                onClick = { selectedImageUri = null },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .background(Color.White, CircleShape)
                                    .size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close, 
                                    contentDescription = "Remove Image", 
                                    tint = Color.Red,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_mors_logo),
                                    contentDescription = null,
                                    modifier = Modifier.size(60.dp),
                                    alpha = 0.5f
                                )
                                Spacer(Modifier.height(8.dp))
                                Text("Click to select an image", color = TextGrey, fontSize = 14.sp)
                                Spacer(Modifier.height(12.dp))
                                Button(
                                    onClick = { imagePickerLauncher.launch("image/*") },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Choose File", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Text("Add Caption", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkNavy)
                    OutlinedTextField(
                        value = caption,
                        onValueChange = { if (it.length <= 500) caption = it },
                        placeholder = { Text("Write a caption...", color = Color.LightGray) },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Text("${caption.length} / 500", color = TextGrey, fontSize = 12.sp, modifier = Modifier.align(Alignment.End))

                    Spacer(Modifier.height(16.dp))

                    Text("Location", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkNavy)
                    OutlinedTextField(
                        value = locationName,
                        onValueChange = { locationName = it },
                        placeholder = { Text("Click icon to tag current location", color = Color.LightGray) },
                        leadingIcon = { 
                            IconButton(onClick = {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                                        .addOnSuccessListener { loc ->
                                            if (loc != null) {
                                                latitude = loc.latitude
                                                longitude = loc.longitude
                                                locationName = "Location Tagged"
                                            }
                                        }
                                } else {
                                    locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                                }
                            }) {
                                Icon(Icons.Default.MyLocation, contentDescription = "Get current location", tint = PrimaryBlue)
                            }
                        },
                        trailingIcon = { 
                            if (locationName.isNotEmpty()) {
                                IconButton(onClick = { 
                                    locationName = ""
                                    latitude = 0.0
                                    longitude = 0.0
                                }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear location", tint = TextGrey)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        readOnly = true
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(
                    onClick = { 
                        selectedImageUri = null
                        caption = ""
                        locationName = ""
                        latitude = 0.0
                        longitude = 0.0
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isPosting
                ) {
                    Text("Reset", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = {
                        if (selectedImageUri == null) {
                            Toast.makeText(context, "Please select an image", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (userId == null) {
                            Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        isPosting = true
                        MediaManager.get().upload(selectedImageUri)
                            .callback(object : UploadCallback {
                                override fun onStart(requestId: String?) {}
                                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                                override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                                    val imageUrl = resultData?.get("secure_url") as? String
                                    if (imageUrl != null) {
                                        val post = hashMapOf(
                                            "userId" to userId,
                                            "imageUrl" to imageUrl,
                                            "caption" to caption,
                                            "locationName" to locationName,
                                            "latitude" to latitude,
                                            "longitude" to longitude,
                                            "timestamp" to Date()
                                        )
                                        db.collection("posts").add(post)
                                            .addOnSuccessListener {
                                                isPosting = false
                                                Toast.makeText(context, "Post successful!", Toast.LENGTH_SHORT).show()
                                                onNavigate(Screen.Feed.route)
                                            }
                                            .addOnFailureListener {
                                                isPosting = false
                                                Toast.makeText(context, "Failed to create post", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                }
                                override fun onError(requestId: String?, error: ErrorInfo?) {
                                    isPosting = false
                                    Toast.makeText(context, "Upload failed: ${error?.description}", Toast.LENGTH_SHORT).show()
                                }
                                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                            })
                            .dispatch()
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isPosting
                ) {
                    if (isPosting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Post", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("© 2026 MORS. All rights reserved.", color = TextGrey, fontSize = 10.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}
