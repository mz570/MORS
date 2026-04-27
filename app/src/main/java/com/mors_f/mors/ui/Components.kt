package com.mors_f.mors.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mors_f.mors.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun Sidebar(selectedScreen: String, onNavigate: (String) -> Unit) {
    ModalDrawerSheet(
        drawerContainerColor = DarkNavy,
        drawerContentColor = Color.White,
        modifier = Modifier.width(280.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("MORS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
            
            Spacer(Modifier.height(40.dp))
            
            SidebarItem(icon = Icons.Default.Home, label = "Home", isSelected = selectedScreen == Screen.Home.route, onClick = { onNavigate(Screen.Home.route) })
            SidebarItem(icon = Icons.Default.AddBox, label = "Upload", isSelected = selectedScreen == Screen.Upload.route, onClick = { onNavigate(Screen.Upload.route) })
            SidebarItem(icon = Icons.AutoMirrored.Filled.List, label = "Feed", isSelected = selectedScreen == Screen.Feed.route, onClick = { onNavigate(Screen.Feed.route) })
            SidebarItem(icon = Icons.Default.Person, label = "Profile", isSelected = selectedScreen == Screen.Profile.route, onClick = { onNavigate(Screen.Profile.route) })
            SidebarItem(icon = Icons.Default.Notifications, label = "Notifications", isSelected = selectedScreen == Screen.Notifications.route, onClick = { onNavigate(Screen.Notifications.route) })
            SidebarItem(icon = Icons.Default.Settings, label = "Settings", isSelected = selectedScreen == Screen.Settings.route, onClick = { onNavigate(Screen.Settings.route) })
        }
    }
}

@Composable
fun SidebarItem(icon: ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
    val contentColor = if (isSelected) PrimaryBlue else Color.White
    val backgroundColor = if (isSelected) Color.White else Color.Transparent

    Surface(
        color = backgroundColor,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = contentColor)
            Spacer(Modifier.width(12.dp))
            Text(label, color = contentColor, fontSize = 16.sp)
        }
    }
}

@Composable
fun MorsTextField(
    label: String, 
    placeholder: String, 
    icon: ImageVector, 
    isPassword: Boolean = false,
    value: String = "",
    onValueChange: (String) -> Unit = {}
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(label, color = TextDark, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color.LightGray) },
            leadingIcon = { Icon(icon, contentDescription = null, tint = Color.Gray) },
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = Color.Gray
                        )
                    }
                }
            } else null,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None
        )
    }
}

@Composable
fun TopBar(title: String, onMenuClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onMenuClick) {
            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = TextDark)
        }
        Text(
            text = "MORS",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = DarkNavy
        )
        Box(modifier = Modifier.size(48.dp))
    }
}

@Composable
fun PostCard(postData: Map<String, Any>, postId: String, canDelete: Boolean = false, onDelete: (() -> Unit)? = null) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid

    val imageUrl = postData["imageUrl"] as? String ?: ""
    val caption = postData["caption"] as? String ?: ""
    val locationName = postData["locationName"] as? String ?: ""
    val lat = postData["latitude"] as? Double ?: 0.0
    val lon = postData["longitude"] as? Double ?: 0.0
    val timestamp = postData["timestamp"] as? com.google.firebase.Timestamp
    val postUserId = postData["userId"] as? String ?: ""

    val upvotedBy = (postData["upvotedBy"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
    val downvotedBy = (postData["downvotedBy"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
    
    val isUpvoted = currentUserId != null && currentUserId in upvotedBy
    val isDownvoted = currentUserId != null && currentUserId in downvotedBy

    var username by remember { mutableStateOf("Loading...") }
    var userProfilePic by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCommentsDialog by remember { mutableStateOf(false) }
    var showVotersDialog by remember { mutableStateOf(false) }
    var voterType by remember { mutableStateOf("upvote") }

    LaunchedEffect(postUserId) {
        db.collection("users").document(postUserId).get()
            .addOnSuccessListener {
                username = it.getString("username") ?: "Unknown"
                userProfilePic = it.getString("profilePicture")
            }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Post") },
            text = { Text("Are you sure you want to delete this post?") },
            confirmButton = {
                TextButton(onClick = {
                    db.collection("posts").document(postId).delete()
                        .addOnSuccessListener {
                            showDeleteDialog = false
                            onDelete?.invoke()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Failed to delete post", Toast.LENGTH_SHORT).show()
                        }
                }) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showCommentsDialog) {
        CommentsDialog(
            postId = postId,
            onDismiss = { showCommentsDialog = false },
            postUserId = postUserId
        )
    }
    
    if (showVotersDialog) {
        VotersDialog(
            userIds = if (voterType == "upvote") upvotedBy else downvotedBy,
            title = if (voterType == "upvote") "Liked By" else "Disliked By",
            onDismiss = { showVotersDialog = false }
        )
    }

    val dateStr = timestamp?.toDate()?.let {
        SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(it)
    } ?: "Just now"

    Surface(
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
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
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(username, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DarkNavy)
                    Text(dateStr, color = TextGrey, fontSize = 12.sp)
                }
                if (canDelete && currentUserId == postUserId) {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                    }
                }
            }

            if (locationName.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .padding(top = 4.dp, bottom = 8.dp)
                        .clickable {
                            if (lat != 0.0 && lon != 0.0) {
                                val uri = "geo:$lat,$lon?q=$lat,$lon(Tagged Location)"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                                context.startActivity(intent)
                            }
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(locationName, color = PrimaryBlue, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }

            if (caption.isNotEmpty()) {
                Text(
                    text = caption,
                    fontSize = 14.sp,
                    color = TextDark,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            if (imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Post Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                InteractionButton(
                    icon = if (isUpvoted) Icons.Default.ThumbUp else Icons.Default.ThumbUpOffAlt, 
                    count = upvotedBy.size.toString(),
                    tint = if (isUpvoted) PrimaryBlue else TextGrey,
                    onLongClick = {
                        voterType = "upvote"
                        showVotersDialog = true
                    },
                    onClick = {
                        if (currentUserId != null) {
                            val postRef = db.collection("posts").document(postId)
                            if (isUpvoted) {
                                postRef.update("upvotedBy", FieldValue.arrayRemove(currentUserId))
                                deleteNotification(postId, currentUserId, postUserId, "like")
                            } else if (!isDownvoted) {
                                postRef.update("upvotedBy", FieldValue.arrayUnion(currentUserId))
                                createNotification(postId, currentUserId, postUserId, "like")
                            }
                        }
                    }
                )
                Spacer(Modifier.width(16.dp))
                InteractionButton(
                    icon = if (isDownvoted) Icons.Default.ThumbDown else Icons.Default.ThumbDownOffAlt, 
                    count = downvotedBy.size.toString(),
                    tint = if (isDownvoted) Color.Red else TextGrey,
                    onLongClick = {
                        voterType = "downvote"
                        showVotersDialog = true
                    },
                    onClick = {
                        if (currentUserId != null) {
                            val postRef = db.collection("posts").document(postId)
                            if (isDownvoted) {
                                postRef.update("downvotedBy", FieldValue.arrayRemove(currentUserId))
                                deleteNotification(postId, currentUserId, postUserId, "dislike")
                            } else if (!isUpvoted) {
                                postRef.update("downvotedBy", FieldValue.arrayUnion(currentUserId))
                                createNotification(postId, currentUserId, postUserId, "dislike")
                            }
                        }
                    }
                )
                Spacer(Modifier.width(16.dp))
                InteractionButton(
                    icon = Icons.AutoMirrored.Filled.Comment, 
                    count = (postData["commentsCount"] as? Long ?: 0).toString(),
                    onClick = { showCommentsDialog = true }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InteractionButton(
    icon: ImageVector, 
    count: String, 
    tint: Color = PrimaryBlue, 
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier.combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick
        )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(4.dp))
            Text(count, color = TextGrey, fontSize = 12.sp)
        }
    }
}

@Composable
fun VotersDialog(userIds: List<String>, title: String, onDismiss: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    var voters by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(userIds) {
        if (userIds.isEmpty()) {
            isLoading = false
        } else {
            val fetchedVoters = mutableListOf<String>()
            userIds.forEach { uid ->
                db.collection("users").document(uid).get()
                    .addOnSuccessListener { doc ->
                        doc.getString("username")?.let { fetchedVoters.add(it) }
                        if (fetchedVoters.size == userIds.size) {
                            voters = fetchedVoters
                            isLoading = false
                        }
                    }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            if (isLoading) {
                CircularProgressIndicator()
            } else if (voters.isEmpty()) {
                Text("No users yet")
            } else {
                LazyColumn {
                    items(voters) { name ->
                        Text(name, modifier = Modifier.padding(vertical = 4.dp), fontWeight = FontWeight.Medium)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun CommentsDialog(postId: String, onDismiss: () -> Unit, postUserId: String) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid
    val context = LocalContext.current
    
    var commentText by remember { mutableStateOf("") }
    var comments by remember { mutableStateOf<List<Pair<String, Map<String, Any>>>>(emptyList()) }
    var isPosting by remember { mutableStateOf(false) }

    LaunchedEffect(postId) {
        db.collection("posts").document(postId).collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { value, error ->
                if (value != null) {
                    comments = value.documents.map { it.id to (it.data ?: emptyMap()) }
                }
            }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Comments") },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(comments) { (commentId, commentData) ->
                        CommentItem(
                            commentId = commentId,
                            commentData = commentData,
                            postId = postId,
                            currentUserId = currentUserId,
                            postUserId = postUserId
                        )
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        placeholder = { Text("Write a comment...", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        maxLines = 2
                    )
                    IconButton(
                        onClick = {
                            if (commentText.isNotBlank() && currentUserId != null) {
                                isPosting = true
                                val newComment = hashMapOf(
                                    "userId" to currentUserId,
                                    "text" to commentText,
                                    "timestamp" to FieldValue.serverTimestamp()
                                )
                                db.collection("posts").document(postId).collection("comments")
                                    .add(newComment)
                                    .addOnSuccessListener {
                                        commentText = ""
                                        isPosting = false
                                        db.collection("posts").document(postId).update("commentsCount", FieldValue.increment(1))
                                        createNotification(postId, currentUserId, postUserId, "comment")
                                    }
                                    .addOnFailureListener {
                                        isPosting = false
                                        Toast.makeText(context, "Failed to post comment", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        },
                        enabled = !isPosting
                    ) {
                        if (isPosting) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        else Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = PrimaryBlue)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun CommentItem(commentId: String, commentData: Map<String, Any>, postId: String, currentUserId: String?, postUserId: String) {
    val db = FirebaseFirestore.getInstance()
    val userId = commentData["userId"] as? String ?: ""
    val text = commentData["text"] as? String ?: ""
    var username by remember { mutableStateOf("Loading...") }

    LaunchedEffect(userId) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { username = it.getString("username") ?: "Unknown" }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = username, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DarkNavy)
            Spacer(Modifier.weight(1f))
            if (userId == currentUserId) {
                IconButton(
                    onClick = {
                        db.collection("posts").document(postId).collection("comments").document(commentId).delete()
                            .addOnSuccessListener {
                                db.collection("posts").document(postId).update("commentsCount", FieldValue.increment(-1))
                                deleteNotification(postId, userId, postUserId, "comment")
                            }
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(16.dp))
                }
            }
        }
        Text(text = text, fontSize = 14.sp, color = TextDark)
        HorizontalDivider(modifier = Modifier.padding(top = 4.dp), color = BackgroundLight)
    }
}

fun createNotification(postId: String, fromUserId: String, targetUserId: String, type: String) {
    if (fromUserId == targetUserId) return
    val db = FirebaseFirestore.getInstance()
    val notification = hashMapOf(
        "postId" to postId,
        "fromUserId" to fromUserId,
        "targetUserId" to targetUserId,
        "type" to type,
        "timestamp" to FieldValue.serverTimestamp()
    )
    db.collection("notifications").add(notification)
}

fun deleteNotification(postId: String, fromUserId: String, targetUserId: String, type: String) {
    val db = FirebaseFirestore.getInstance()
    db.collection("notifications")
        .whereEqualTo("postId", postId)
        .whereEqualTo("fromUserId", fromUserId)
        .whereEqualTo("targetUserId", targetUserId)
        .whereEqualTo("type", type)
        .get()
        .addOnSuccessListener { documents ->
            for (document in documents) {
                db.collection("notifications").document(document.id).delete()
            }
        }
}
