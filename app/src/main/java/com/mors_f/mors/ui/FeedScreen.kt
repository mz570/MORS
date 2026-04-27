package com.mors_f.mors.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mors_f.mors.R
import com.mors_f.mors.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(onNavigate: (String) -> Unit) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val db = FirebaseFirestore.getInstance()
    
    var posts by remember { mutableStateOf<List<Pair<String, Map<String, Any>>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    
    val connectionState by connectivityState()

    fun fetchPosts() {
        isLoading = true
        db.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { value ->
                posts = value.documents.map { it.id to (it.data ?: emptyMap()) }
                isLoading = false
                isRefreshing = false
            }
            .addOnFailureListener {
                isLoading = false
                isRefreshing = false
            }
    }

    LaunchedEffect(Unit) {
        fetchPosts()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Sidebar(
                selectedScreen = Screen.Feed.route,
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
                title = "Feed",
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
                    text = "Feed",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DarkNavy
                )
            }

            if (connectionState == ConnectionState.Unavailable) {
                NetworkErrorUI { fetchPosts() }
            } else {
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = {
                        isRefreshing = true
                        fetchPosts()
                    },
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (isLoading && !isRefreshing) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = PrimaryBlue)
                        }
                    } else if (posts.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.Center) {
                            Text("No posts yet. Be the first to share!", color = TextGrey)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(20.dp),
                            contentPadding = PaddingValues(bottom = 20.dp)
                        ) {
                            items(posts) { (postId, postData) ->
                                PostCard(postData = postData, postId = postId)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NetworkErrorUI(onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.WifiOff,
                contentDescription = "No Internet",
                modifier = Modifier.size(80.dp),
                tint = Color.Red.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Network not found",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = DarkNavy
            )
            Text(
                "Please check your connection and try again.",
                fontSize = 14.sp,
                color = TextGrey,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Try Again")
            }
        }
    }
}
