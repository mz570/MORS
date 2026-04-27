package com.mors_f.mors.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mors_f.mors.R
import com.mors_f.mors.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NotificationsScreen(onNavigate: (String) -> Unit) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUserId = auth.currentUser?.uid

    var notifications by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            db.collection("notifications")
                .whereEqualTo("targetUserId", currentUserId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { value, error ->
                    if (value != null) {
                        notifications = value.documents.map { it.data ?: emptyMap() }
                    }
                    isLoading = false
                }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Sidebar(
                selectedScreen = Screen.Notifications.route,
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
                title = "Notifications",
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
                    text = "Notifications",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DarkNavy
                )
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            } else if (notifications.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(64.dp), tint = TextGrey)
                        Text("No notifications yet", color = TextGrey, fontSize = 16.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    items(notifications) { notification ->
                        NotificationItem(notification)
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notification: Map<String, Any>) {
    val db = FirebaseFirestore.getInstance()
    val fromUserId = notification["fromUserId"] as? String ?: ""
    val type = notification["type"] as? String ?: ""
    val timestamp = notification["timestamp"] as? com.google.firebase.Timestamp
    
    var username by remember { mutableStateOf("...") }
    var userProfilePic by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(fromUserId) {
        db.collection("users").document(fromUserId).get()
            .addOnSuccessListener {
                username = it.getString("username") ?: "User"
                userProfilePic = it.getString("profilePicture")
            }
    }

    val dateStr = timestamp?.toDate()?.let {
        SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(it)
    } ?: ""

    val message = when (type) {
        "like" -> "liked your post"
        "dislike" -> "disliked your post"
        "comment" -> "commented on your post"
        else -> "interacted with you"
    }

    Surface(
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(BackgroundLight),
                contentAlignment = Alignment.Center
            ) {
                if (userProfilePic != null) {
                    AsyncImage(
                        model = userProfilePic,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryBlue)
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = DarkNavy)) {
                            append(username)
                        }
                        append(" $message")
                    },
                    fontSize = 14.sp,
                    color = TextDark
                )
                Text(text = dateStr, fontSize = 12.sp, color = TextGrey)
            }
        }
    }
}
