# MORS - Social Media & Location Discovery Platform

> **MORS** stands for **M**oment-**O**riented **R**eal-time **S**ocial platform. A modern Android application that combines social networking with geolocation features, allowing users to share moments with media, discover posts by location, and interact through likes, dislikes, and comments.

![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?logo=kotlin)
![Android](https://img.shields.io/badge/Android-API%2024%2B-3DDC84?logo=android)
![Firebase](https://img.shields.io/badge/Firebase-Latest-FFA500?logo=firebase)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-2024.09-4285F4)

---

## 📋 Table of Contents

1. [Project Overview](#project-overview)
2. [Tech Stack](#tech-stack)
3. [Architecture](#architecture)
4. [Features](#features)
5. [Database Schema](#database-schema)
6. [Mobile App Structure](#mobile-app-structure)
7. [Backend & APIs](#backend--apis)
8. [Frontend Design](#frontend-design)
9. [System Connections](#system-connections)
10. [Installation & Setup](#installation--setup)
11. [Development Guide](#development-guide)
12. [Project Structure](#project-structure)
13. [Contributing](#contributing)
14. [Resources](#resources)

---

## Project Overview

MORS is a **real-time social media application** for Android (API 24+) that emphasizes location-based content sharing and user interactions. The app leverages Firebase for authentication, data storage, and real-time updates, with Cloudinary for image hosting.

### Key Objectives
- Enable users to share moments via image posts with captions
- Attach geographic location data to posts for discovery
- Provide real-time interaction feedback (likes, dislikes, comments)
- Send push notifications for user interactions
- Maintain user profiles with authentication security

### Target Users
- Social media enthusiasts
- Location-based community members
- Photography and moment-sharing enthusiasts

---

## Tech Stack

### Frontend
| Layer | Technology | Version | Purpose |
|-------|-----------|---------|---------|
| **UI Framework** | Jetpack Compose | 2024.09 | Modern declarative UI toolkit |
| **Language** | Kotlin | 2.0.21 | Type-safe JVM language |
| **Navigation** | Jetpack Navigation Compose | 2.9.8 | In-app navigation & routing |
| **Lifecycle** | Lifecycle Runtime | 2.10.0 | Activity/Fragment lifecycle management |
| **Image Loading** | Coil | 2.7.0 | Asynchronous image loading & caching |
| **Material Design** | Material3 | Latest | Google Material Design 3 components |

### Backend Services
| Service | Type | Purpose | Configuration |
|---------|------|---------|----------------|
| **Firebase Authentication** | BaaS | User sign-up, login, session management | Email/Password & Google OAuth |
| **Firestore** | NoSQL Database | Real-time data storage & synchronization | Document-based, real-time listeners |
| **Cloud Storage** | CDN | Media file storage (alternative to Cloudinary) | Integrated with Firebase |
| **Google Play Services** | SDK | Location services & authentication | GPS location detection |
| **Cloudinary** | Image CDN | Image upload & optimization | Image hosting & transformations |

### Build & Dependencies
| Tool | Version | Purpose |
|------|---------|---------|
| **AGP** | 9.0.1 | Android Gradle Plugin |
| **SDK** | 36 | Target SDK (Android 15) |
| **Min SDK** | 24 | Android 7.0 & above |
| **Gradle** | 8.x | Build automation |

---

## Architecture

### High-Level Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                    MORS Android Application                     │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │              PRESENTATION LAYER (UI)                     │  │
│  │  ┌────────────┬────────────┬────────────┬────────────┐  │  │
│  │  │    Home    │    Feed    │   Upload   │  Profile   │  │  │
│  │  ├────────────┼────────────┼────────────┼────────────┤  │  │
│  │  │  Account   │ Settings   │   Notifs   │  Logout    │  │  │
│  │  └────────────┴────────────┴────────────┴────────────┘  │  │
│  │                      (Jetpack Compose)                   │  │
│  └──────────────────────────────────────────────────────────┘  │
│                              ▲                                  │
│                              │ Navigation Events               │
│                              ▼                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │           BUSINESS LOGIC LAYER (Services)                │  │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐ │  │
│  │  │  Auth    │  │ Post     │  │ Comment  │  │ Notif    │ │  │
│  │  │ Service  │  │ Service  │  │ Service  │  │ Service  │ │  │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘ │  │
│  │              (Firebase SDK Wrappers)                      │  │
│  └──────────────────────────────────────────────────────────┘  │
│                              ▲                                  │
│                              │ API Calls                       │
│                              ▼                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │            DATA LAYER (Repositories)                     │  │
│  │  ┌──────────────────────────────────────────────────┐   │  │
│  │  │     Firebase Firestore Integration Layer         │   │  │
│  │  └──────────────────────────────────────────────────┘   │  │
│  └──────────────────────────────────────────────────────────┘  │
│                              ▲                                  │
└──────────────────────────────┼──────────────────────────────────┘
                               │
        ┌──────────────────────┼──────────────────────┐
        │                      │                      │
        ▼                      ▼                      ▼
   ┌─────────────┐        ┌─────────────┐      ┌──────────────┐
   │   Firebase  │        │ Cloudinary  │      │ Google Play  │
   │   Backend   │        │   CDN       │      │  Services    │
   │ (Auth,DB)   │        │  (Images)   │      │ (Location)   │
   └─────────────┘        └─────────────┘      └──────────────┘
```

### Architectural Pattern: MVVM (Model-View-ViewModel)

```
┌──────────────────────────────────────────────────┐
│              SCREEN (View)                       │
│          Composable Functions                    │
│  - StateHolders (rememberCoroutineScope)         │
│  - State Variables (remember { mutableStateOf }) │
│  - LaunchedEffect Handlers                       │
└────────────────────────┬─────────────────────────┘
                         │ (Observes)
                         ▼
┌──────────────────────────────────────────────────┐
│        SCREEN LOGIC (ViewModel)                  │
│  - Firebase.getInstance() calls                  │
│  - State Management (var variables)              │
│  - Business Logic (LaunchedEffect blocks)        │
│  - Event Handling (onClick callbacks)            │
└────────────────────────┬─────────────────────────┘
                         │ (Queries/Writes)
                         ▼
┌──────────────────────────────────────────────────┐
│         MODEL (Data Repository)                  │
│  - FirebaseAuth Instance                         │
│  - FirebaseFirestore Instance                    │
│  - Firestore Collections (USERS, POSTS, etc.)    │
│  - Real-time Listeners & Snapshots               │
└──────────────────────────────────────────────────┘
```

### Data Flow Example (Create Post)

```
User Action: User clicks "Post" button in UploadScreen
                              │
                              ▼
              UploadScreen composable function
              (caption, imageUrl set in state)
                              │
                              ▼
              User clicks "Post" → onClick handler
                              │
                              ▼
              Upload image to Cloudinary
              (MediaManager.upload())
                              │
                              ▼
              OnSuccess: Create Firestore document
              db.collection("posts").add(postData)
                              │
                              ▼
              Update local state: isPosting = false
              Navigate to Feed screen
                              │
                              ▼
              FeedScreen LaunchedEffect triggers
              db.collection("posts").get() with listener
                              │
                              ▼
              Posts displayed in LazyColumn
              with PostCard composables
```

---

## Features

### 1. **Authentication System**
- ✅ Email/Password Registration
- ✅ Email/Password Login
- ✅ Google OAuth Login (via Play Services)
- ✅ Firebase Session Management
- ✅ Automatic Login on App Restart

### 2. **Post Management**
- ✅ Create posts with image upload
- ✅ Add captions to posts
- ✅ Geo-tag posts with location & coordinates
- ✅ View user feed (all posts, descending by timestamp)
- ✅ Delete own posts
- ✅ Denormalized comment count display

### 3. **Social Interactions**
- ✅ Upvote/Like posts
- ✅ Downvote/Dislike posts
- ✅ Write comments on posts
- ✅ View all comments on a post
- ✅ Delete own comments
- ✅ Real-time vote count updates

### 4. **Notifications**
- ✅ Receive notifications for likes
- ✅ Receive notifications for dislikes
- ✅ Receive notifications for comments
- ✅ View notification history
- ✅ See who interacted and when

### 5. **User Profiles**
- ✅ View personal profile
- ✅ Upload/change profile picture
- ✅ View personal posts
- ✅ Edit account information (name, username, email, password)
- ✅ Delete posts from profile

### 6. **Settings & Account**
- ✅ Account information management
- ✅ Password change
- ✅ Logout functionality
- ✅ App settings

### 7. **Offline & Network Handling**
- ✅ Offline detection
- ✅ Manual feed refresh
- ✅ Network connectivity state monitoring

---

## Database Schema

For detailed database structure, relationships, and indexes, see **[ER_DIAGRAM.md](./ER_DIAGRAM.md)**.

### Collections Overview

```
MORS Firestore Database
├── users/
│   └── {uid}
│       ├── firstName: String
│       ├── lastName: String
│       ├── username: String ✓ Unique
│       ├── email: String ✓ Unique
│       ├── phoneNumber: String
│       ├── profilePicture: String (optional)
│       └── createdAt: Timestamp
│
├── posts/
│   └── {postId}
│       ├── userId: String → users.uid
│       ├── caption: String
│       ├── imageUrl: String
│       ├── locationName: String
│       ├── latitude: Double
│       ├── longitude: Double
│       ├── timestamp: Timestamp
│       ├── upvotedBy: Array<String>
│       ├── downvotedBy: Array<String>
│       ├── commentsCount: Long
│       └── comments/ (Subcollection)
│           └── {commentId}
│               ├── userId: String → users.uid
│               ├── text: String
│               └── timestamp: Timestamp
│
└── notifications/
    └── {notificationId}
        ├── toUserId: String → users.uid
        ├── fromUserId: String → users.uid
        ├── type: String (enum: like|dislike|comment)
        ├── postId: String → posts.postId
        ├── timestamp: Timestamp
        └── read: Boolean (optional)
```

### Key Queries

**Fetch all posts (feed):**
```kotlin
db.collection("posts")
    .orderBy("timestamp", Query.Direction.DESCENDING)
    .get()
```

**Fetch user's posts:**
```kotlin
db.collection("posts")
    .whereEqualTo("userId", userId)
    .orderBy("timestamp", Query.Direction.DESCENDING)
    .get()
```

**Fetch user notifications:**
```kotlin
db.collection("notifications")
    .whereEqualTo("toUserId", userId)
    .orderBy("timestamp", Query.Direction.DESCENDING)
    .addSnapshotListener { ... }
```

**Fetch post comments:**
```kotlin
db.collection("posts")
    .document(postId)
    .collection("comments")
    .orderBy("timestamp", Query.Direction.ASCENDING)
    .addSnapshotListener { ... }
```

---

## Mobile App Structure

### Screen Navigation Map

```
┌─────────────────────────────────────────────────────────┐
│                  MORS App Navigation                    │
└─────────────────────────────────────────────────────────┘

┌─── UNAUTHENTICATED STATE
│
├── Login Screen
│   ├── Email/Password Input
│   ├── "Forgot Password" Link
│   ├── Google Sign-In Button
│   └── "Register" Link → Register Screen
│
└── Register Screen
    ├── First Name, Last Name Input
    ├── Email Input
    ├── Username Input (unique)
    ├── Phone Number Input
    ├── Password & Confirm Password
    └── "Register" Button → Home (if auth successful)

┌─── AUTHENTICATED STATE
│
├── Feed Screen (Default)
│   ├── TopBar with Menu
│   ├── Pull-to-Refresh
│   ├── LazyColumn of PostCards
│   │   ├── Post Image & Caption
│   │   ├── Location Tag
│   │   ├── Upvote/Downvote/Comment Buttons
│   │   └── View Comments Dialog
│   ├── Sidebar Navigation (Modal Drawer)
│   │   ├── Home
│   │   ├── Feed
│   │   ├── Upload Post
│   │   ├── Profile
│   │   ├── Notifications
│   │   ├── Settings
│   │   ├── Account Info
│   │   └── Logout
│   └── Network Offline State
│
├── Home Screen
│   └── (Alternative main screen with dashboard)
│
├── Upload Screen
│   ├── Image Picker
│   ├── Caption TextArea
│   ├── Location Permission & Fetch
│   ├── Location Display (Lat/Lon)
│   ├── "Post" Button
│   ├── Upload Progress Indicator
│   └── Success/Error Messages
│
├── Profile Screen
│   ├── User Info (Name, Username, Email)
│   ├── Profile Picture (with upload option)
│   ├── User's Posts LazyColumn
│   ├── Post Deletion with Confirmation
│   ├── Sidebar with Navigation
│   └── Pull-to-Refresh
│
├── Account Info Screen
│   ├── First Name, Last Name Fields (editable)
│   ├── Username Field (editable)
│   ├── Email Field (display only)
│   ├── Password Change Field
│   ├── "Save Changes" Button
│   ├── Success/Error Messages
│   └── Sidebar Navigation
│
├── Notifications Screen
│   ├── Notification List (LazyColumn)
│   │   ├── From User Avatar
│   │   ├── "User X liked/disliked/commented on your post"
│   │   ├── Timestamp
│   │   └── Interactive post preview
│   ├── Empty State (no notifications)
│   ├── Loading State
│   └── Sidebar Navigation
│
├── Settings Screen
│   ├── Version Info
│   ├── About MORS
│   ├── Preferences (expandable)
│   ├── App Settings
│   └── Sidebar Navigation
│
├── Logout Screen
│   ├── Confirmation Dialog
│   ├── "Yes, Logout" Button → Clear session & navigate to Login
│   └── "Cancel" Button → Go Back
│
└── Components (Reusable)
    ├── TopBar (with menu icon & title)
    ├── Sidebar (navigation drawer)
    ├── PostCard (post display & interactions)
    ├── InteractionButton (upvote/downvote/comment)
    ├── CommentDialog (view & add comments)
    └── VotersDialog (view who upvoted/downvoted)
```

### Screen Components Details

#### **Feed Screen**
- Displays all posts from all users
- Real-time updates via Firestore listener
- Pull-to-refresh functionality
- Offline state with retry button
- Each PostCard shows: author, image, caption, location, engagement (upvotes/downvotes/comments)

#### **Upload Screen**
- Image picker from device gallery
- Automatic location detection via Google Play Services
- Displays current location coordinates
- Caption input with character counter
- Progress indicator during upload
- Cloudinary integration for image upload

#### **Profile Screen**
- User profile information display
- Profile picture upload
- List of user's own posts
- Delete post functionality with confirmation
- Edit profile button → Account Info Screen

#### **Notifications Screen**
- List of all interactions (likes, dislikes, comments)
- Sorted by timestamp (newest first)
- Shows interaction type and user who interacted
- No read/unread distinction currently (can be added)

---

## Backend & APIs

### Firebase Integration

#### **Firebase Authentication**
```kotlin
val auth = FirebaseAuth.getInstance()

// Create account
auth.createUserWithEmailAndPassword(email, password)
    .addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val user = auth.currentUser
            val userId = user?.uid
        }
    }

// Login
auth.signInWithEmailAndPassword(email, password)
    .addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val user = auth.currentUser
        }
    }

// Listen to auth state changes
val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
    val currentUser = firebaseAuth.currentUser
}
auth.addAuthStateListener(listener)

// Logout
auth.signOut()
```

#### **Firestore Database Operations**

**Read Operations:**
```kotlin
val db = FirebaseFirestore.getInstance()

// Get single document
db.collection("users").document(userId).get()
    .addOnSuccessListener { document ->
        val data = document.data
    }

// Query with listener (real-time)
db.collection("posts")
    .orderBy("timestamp", Query.Direction.DESCENDING)
    .addSnapshotListener { snapshots, error ->
        val posts = snapshots?.documents?.map { it.data }
    }
```

**Write Operations:**
```kotlin
// Create document
val userMap = hashMapOf(
    "firstName" to firstName,
    "email" to email,
    // ... other fields
)
db.collection("users").document(userId).set(userMap)
    .addOnSuccessListener { /* Success */ }

// Update document
val updates = mapOf(
    "firstName" to newFirstName,
    "profilePicture" to newImageUrl
)
db.collection("users").document(userId).update(updates)

// Delete document
db.collection("posts").document(postId).delete()

// Add to array
db.collection("posts").document(postId).update(
    "upvotedBy", FieldValue.arrayUnion(userId)
)

// Remove from array
db.collection("posts").document(postId).update(
    "upvotedBy", FieldValue.arrayRemove(userId)
)
```

#### **Batch Operations:**
```kotlin
// Create notification and update comment count
val batch = db.batch()
batch.set(db.collection("notifications").document(), notificationData)
batch.update(db.collection("posts").document(postId), "commentsCount", increment)
batch.commit()
    .addOnSuccessListener { /* Done */ }
```

### Cloudinary Image Upload Integration

```kotlin
val config = hashMapOf(
    "cloud_name" to "your_cloud_name",
    "api_key" to "your_api_key"
)
MediaManager.init(context, config)

MediaManager.upload(imageUri)
    .callback(object : UploadCallback {
        override fun onSuccess(resultData: MutableMap<*, *>?) {
            val imageUrl = resultData?.get("secure_url") as? String
        }
        override fun onError(error: ErrorInfo?) {
            // Handle error
        }
    })
    .start()
```

### Google Play Services - Location

```kotlin
val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
    .addOnSuccessListener { location ->
        val latitude = location.latitude
        val longitude = location.longitude
    }
```

### External APIs Used

| API | Purpose | Authentication |
|-----|---------|-----------------|
| **Firebase Auth REST API** | User authentication | API Key (google-services.json) |
| **Firestore REST API** | Database operations | Firebase Credentials |
| **Google Play Services API** | Location services | API Key in google-services.json |
| **Cloudinary API** | Image upload & hosting | Cloud Name + API Key |

---

## Frontend Design

### Color Scheme
```kotlin
val PrimaryBlue = Color(0xFF007AFF)      // Primary action color
val DarkNavy = Color(0xFF1A1A3E)        // Dark backgrounds
val BackgroundLight = Color(0xFFF5F5F7)  // Light backgrounds
val TextDark = Color(0xFF000000)         // Primary text
val TextGrey = Color(0xFF666666)         // Secondary text
val ErrorRed = Color(0xFFFF3B30)         // Error state
val SuccessGreen = Color(0xFF34C759)     // Success state
```

### Typography
- **Headlines**: Font weight Bold, 20sp
- **Body Text**: Regular, 14-16sp
- **Small Text**: Regular, 12sp
- **Button Text**: Semi-bold, 14sp

### Layout Grid
- Standard padding: 16dp, 24dp
- Standard spacing: 8dp, 12dp, 16dp
- Border radius (cards): 16dp
- Icon size: 24dp (standard), 48dp (avatar)

### Material Design 3 Components Used
- `Card` & `Surface`
- `Button` & `IconButton`
- `TextField` & `OutlinedTextField`
- `TopAppBar`
- `NavigationDrawer` / `ModalNavigationDrawer`
- `AlertDialog`
- `LazyColumn` / `LazyRow`
- `Icon` & `Image`
- `Chip` (for text tags)

---

## System Connections

### Data Flow Architecture Diagram

```
┌──────────────────────────────┐
│   User Device (Android)      │
│  ┌────────────────────────┐  │
│  │   MORS App (Kotlin)    │  │
│  │  - Jetpack Compose UI  │  │
│  │  - Firebase SDK        │  │
│  │  - Cloudinary SDK      │  │
│  │  - Play Services       │  │
│  └────────────────────────┘  │
└──────────────┬───────────────┘
               │
        ┌──────┴─────┬──────────┬────────────┐
        │            │          │            │
        ▼            ▼          ▼            ▼
    ┌────────┐  ┌─────────┐ ┌──────────┐ ┌──────────┐
    │Firebase│  │Cloudinary│ │ Google   │ │  GPS/   │
    │ Auth   │  │  API     │ │Play Svc  │ │Location │
    └────────┘  └─────────┘ └──────────┘ └──────────┘
        │            │          │
        └──────┬─────┴──────┬───┘
               │            │
               ▼            ▼
         ┌──────────┐   ┌──────────┐
         │Firestore │   │Image CDN │
         │Database  │   │(Storage) │
         └──────────┘   └──────────┘
```

### API Request/Response Flow

**Create Post Request:**
```
Client Request
    │
    ├─ 1. Upload image → Cloudinary
    │       └─ Response: imageUrl
    │
    ├─ 2. Get location → Google Play Services
    │       └─ Response: latitude, longitude
    │
    ├─ 3. Create Firestore document
    │       POST /posts with imageUrl + location
    │       └─ Response: postId, timestamp
    │
    └─ 4. Navigate to Feed (auto-refresh)
            └─ Fetch posts → Firestore
                Response: updated posts list
```

### Real-time Data Synchronization

```
Firestore Listener Active (FeedScreen)
    │
    └─ db.collection("posts").addSnapshotListener()
        │
        └─ On Data Change (QuerySnapshot)
            │
            ├─ Type.ADDED → New post
            │   └─ Update UI with new PostCard
            │
            ├─ Type.MODIFIED → Post updated (vote/comment count)
            │   └─ Update specific PostCard
            │
            ├─ Type.REMOVED → Post deleted
            │   └─ Remove PostCard from UI
            │
            └─ Update last fetch timestamp
```

### Authentication Flow

```
┌─ User launches app
│
├─ Check FirebaseAuth.currentUser
│   │
│   ├─ If null → Navigate to Login Screen
│   │
│   └─ If not null → Navigate to Feed Screen
│
└─ Listen to AuthStateListener
    │
    ├─ ON_LOGIN → Update currentUser state
    │   └─ Navigate to Feed
    │
    ├─ ON_LOGOUT → Clear currentUser
    │   └─ Navigate to Login
    │
    └─ ON_ERROR → Show error message
```

---

## Installation & Setup

### Prerequisites
- Android Studio (Latest)
- JDK 11+
- Android SDK (API 24+)
- Google account (for Firebase)
- Cloudinary account
- Git

### Step 1: Clone the Repository

```bash
git clone https://github.com/yourusername/MORS.git
cd MORS
```

### Step 2: Firebase Setup

1. **Create Firebase Project:**
   - Go to [Firebase Console](https://console.firebase.google.com)
   - Create new project: `mors-b93e1`
   - Enable Google Analytics (optional)

2. **Enable Authentication:**
   - Go to Authentication → Sign-in method
   - Enable Email/Password
   - Enable Google Sign-in (requires Project ID)

3. **Create Firestore Database:**
   - Go to Firestore Database → Create Database
   - Start in production mode
   - Choose region: `us-central1` (recommended)

4. **Download Google Services Configuration:**
   - Go to Project Settings → General
   - Download `google-services.json`
   - Place in `app/` directory

5. **Security Rules (Copy to Firestore):**
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Users collection - own profile readable/writable
    match /users/{userId} {
      allow read, write: if request.auth.uid == userId;
      allow read: if request.auth != null;
    }
    
    // Posts collection - public read, own post writable
    match /posts/{postId} {
      allow read: if request.auth != null;
      allow create, write, delete: if request.auth.uid == resource.data.userId;
      
      // Comments subcollection
      match /comments/{commentId} {
        allow read: if request.auth != null;
        allow create: if request.auth != null;
        allow delete: if request.auth.uid == resource.data.userId;
      }
    }
    
    // Notifications - own notifications readable
    match /notifications/{notificationId} {
      allow read, create, write: if request.auth.uid == resource.data.toUserId;
      allow create: if request.auth != null;
    }
  }
}
```

### Step 3: Cloudinary Setup

1. **Create Cloudinary Account:**
   - Sign up at [Cloudinary](https://cloudinary.com)
   - Verify email

2. **Get Credentials:**
   - Go to Dashboard → Settings
   - Copy Cloud Name and API Key

3. **Configure in App:**
   - Open `app/build.gradle.kts`
   - Dependencies already include: `implementation(libs.cloudinary.android)`

4. **Initialize in Code:**
   - The app initializes Cloudinary in `UploadScreen.kt`
   - Update `cloud_name` and `api_key` in `MediaManager.init()` call

### Step 4: Google Play Services Setup

1. **Generate SHA-1 Certificate:**
```bash
./gradlew signingReport
```

2. **Add to Firebase:**
   - Firebase Console → Project Settings → General
   - Scroll to "Your Apps" → Add fingerprint
   - Paste SHA-1 from signingReport

3. **Enable Play Services APIs:**
   - Google Cloud Console → APIs & Services
   - Enable: Google Play Services Auth, Location API

### Step 5: Build & Run

```bash
# Sync Gradle Files
./gradlew sync

# Build and install on emulator/device
./gradlew installDebug

# Or use Android Studio: Build → Build APK(s)
```

---

## Development Guide

### Project Structure

```
MORS/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml          # App configuration
│   │   ├── java/com/mors_f/mors/
│   │   │   ├── MainActivity.kt          # Entry point, navigation setup
│   │   │   ├── MorsApplication.kt       # Application class (optional)
│   │   │   └── ui/
│   │   │       ├── Navigation.kt        # Screen routes (sealed class)
│   │   │       ├── Components.kt        # Reusable composables
│   │   │       ├── theme/
│   │   │       │   ├── Color.kt         # Color definitions
│   │   │       │   ├── Type.kt          # Typography
│   │   │       │   └── Theme.kt         # Theme composition
│   │   │       ├── LoginScreen.kt       # Login UI + Firebase auth
│   │   │       ├── RegisterScreen.kt    # Registration UI
│   │   │       ├── FeedScreen.kt        # Main feed display
│   │   │       ├── HomeScreen.kt        # Home dashboard
│   │   │       ├── UploadScreen.kt      # Post creation + image upload
│   │   │       ├── ProfileScreen.kt     # User profile display
│   │   │       ├── AccountInfoScreen.kt # Account settings
│   │   │       ├── NotificationsScreen.kt # Notifications list
│   │   │       ├── SettingsScreen.kt    # App settings
│   │   │       ├── LogoutScreen.kt      # Logout confirmation
│   │   │       └── NetworkUtils.kt      # Network connectivity checks
│   │   └── res/
│   │       ├── drawable/                # App icons & images
│   │       ├── values/
│   │       │   ├── strings.xml          # String resources
│   │       │   ├── dimens.xml           # Dimension values
│   │       │   └── colors.xml           # Color resources
│   │       └── xml/
│   │           ├── backup_rules.xml
│   │           └── data_extraction_rules.xml
│   │
│   ├── build.gradle.kts                 # App-level Gradle config
│   ├── proguard-rules.pro               # ProGuard rules
│   └── google-services.json             # Firebase config
│
├── gradle/
│   ├── libs.versions.toml               # Version catalog
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
│
├── build.gradle.kts                     # Root Gradle config
├── settings.gradle.kts                  # Gradle settings
└── gradlew / gradlew.bat                # Gradle wrapper

```

### Key Files Explanation

| File | Purpose |
|------|---------|
| `MainActivity.kt` | Sets up Compose, Firebase auth listener, main NavHost |
| `Navigation.kt` | Defines all screen routes as sealed class |
| `Components.kt` | Reusable Composables: PostCard, Sidebar, TopBar, etc. |
| `*Screen.kt` | Individual screen UIs with Firebase integration |
| `NetworkUtils.kt` | Connectivity state monitoring |
| `theme/` | Centralized UI styling (colors, typography) |

### Common Development Tasks

#### **Add a New Screen**

1. Create new file `NewScreen.kt` in `ui/` directory
2. Define composable function:
```kotlin
@Composable
fun NewScreen(onNavigate: (String) -> Unit) {
    // Your UI code
}
```
3. Add route to `Navigation.kt`:
```kotlin
object NewScreen : Screen("new_screen")
```
4. Add composable to NavHost in `MainActivity.kt`:
```kotlin
composable(Screen.NewScreen.route) {
    NewScreen(onNavigate = { route -> navController.navigate(route) })
}
```

#### **Access Firebase in a Composable**

```kotlin
val auth = FirebaseAuth.getInstance()
val db = FirebaseFirestore.getInstance()
val userId = auth.currentUser?.uid

LaunchedEffect(userId) {
    if (userId != null) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                // Handle data
            }
    }
}
```

#### **Debug Firestore Queries**

Enable Firestore logging in your app:
```kotlin
// In MainActivity or Application class
FirebaseFirestore.setLoggingEnabled(BuildConfig.DEBUG)
```

#### **Handle State Management**

Use `remember { mutableStateOf() }` for screen-level state:
```kotlin
var isLoading by remember { mutableStateOf(false) }
var userData by remember { mutableStateOf<Map<String, Any>?>(null) }

// Update state in LaunchedEffect
LaunchedEffect(userId) {
    isLoading = true
    // Perform work
    isLoading = false
}

// Conditional UI based on state
if (isLoading) {
    LoadingSpinner()
} else if (userData != null) {
    DisplayData(userData!!)
}
```

---

## Contributing

### Development Workflow

1. **Fork the repository** and clone locally
2. **Create a feature branch:**
   ```bash
   git checkout -b feature/your-feature-name
   ```
3. **Make changes** and test thoroughly
4. **Commit with clear messages:**
   ```bash
   git commit -m "feat: add user profile image upload"
   ```
5. **Push to your fork:**
   ```bash
   git push origin feature/your-feature-name
   ```
6. **Create Pull Request** with description and screenshots

### Code Style Guidelines

- Use Kotlin naming conventions (camelCase for variables)
- Add comments for complex logic
- Format code with Android Studio (Ctrl+Alt+L)
- Follow Material Design 3 guidelines for UI
- Write meaningful variable names: `isLoadingPosts` not `il`

### Testing

Currently, the app includes:
- Unit tests: `app/src/test/`
- Instrumented tests: `app/src/androidTest/`

Add tests for new business logic.

### Known Issues & ToDo

- [ ] Implement offline caching for posts
- [ ] Add Cloud Functions for advanced notifications
- [ ] Implement user follow/unfollow system
- [ ] Add search functionality
- [ ] Implement message/DM system
- [ ] Add video post support
- [ ] Optimize image loading (pagination)
- [ ] Add analytics tracking
- [ ] Implement content moderation

---

## Resources

### Official Documentation
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Firebase Documentation](https://firebase.google.com/docs)
- [Firestore Data Modeling](https://firebase.google.com/docs/firestore/data-model)
- [Material Design 3](https://m3.material.io/)
- [Android Dev Guide](https://developer.android.com/)

### Libraries & Tools
- [Coil Image Loading](https://coil-kt.github.io/coil/)
- [Cloudinary Documentation](https://cloudinary.com/documentation)
- [Google Play Services](https://developers.google.com/android/guides/overview)
- [Gradle Documentation](https://gradle.org/guides/)

### Community
- [Android Developers Community](https://developer.android.com/community)
- [Firebase Community](https://firebase.community/)
- [Kotlin Forums](https://kotlinlang.org/community/)

### Useful Tools
- Android Studio Profiler (Debug → Profiler)
- Firebase Console (Project Analytics & Debugging)
- Firestore Emulator (Local testing)
- Logcat (Android Studio → cat filter)

---

## License

This project is licensed under the MIT License - see the LICENSE file for details.

---

## Contact & Support

**Developer:** MORS Team

**Issues & Feature Requests:** [GitHub Issues](https://github.com/yourusername/MORS/issues)

**Questions?** Reach out on the community forums or create a discussion on GitHub.

---

## Changelog

### Version 1.0 (Current)
- Initial release with core features
- Authentication system (Email/Password + Google OAuth)
- Post creation with location tagging
- Like/Dislike and comment system
- User profiles
- Real-time notifications

### Future Releases
- v1.1: Offline mode & caching
- v1.2: User following system
- v1.3: Direct messaging
- v2.0: Video posting & Stories

---

**Last Updated:** April 29, 2026  
**Maintained By:** MORS Development Team  
**Status:** Active Development

---

## Quick Reference Card

```
╔═══════════════════════════════════════════════════════════════╗
║              MORS APP - QUICK REFERENCE                       ║
╠═══════════════════════════════════════════════════════════════╣
║                                                               ║
║  TECH STACK:                                                  ║
║  • Language: Kotlin 2.0.21                                    ║
║  • UI: Jetpack Compose 2024.09                                ║
║  • Backend: Firebase (Auth + Firestore)                       ║
║  • Media: Cloudinary                                          ║
║  • Location: Google Play Services                             ║
║                                                               ║
║  KEY FILES:                                                   ║
║  ├── MainActivity.kt → App entry & navigation                 ║
║  ├── Components.kt → Reusable UI components                   ║
║  ├── *Screen.kt → Individual screen UIs                       ║
║  └── theme/ → Color & typography definitions                 ║
║                                                               ║
║  MAIN SCREENS:                                                ║
║  • Feed → View all posts (real-time)                          ║
║  • Upload → Create new post with image                        ║
║  • Profile → View own posts & info                            ║
║  • Notifications → View interactions                          ║
║  • Account Info → Edit profile settings                       ║
║                                                               ║
║  DATABASE COLLECTIONS:                                        ║
║  • users → User profiles                                      ║
║  • posts → Posts with interactions                            ║
║  • posts/{id}/comments → Post comments                        ║
║  • notifications → User notifications                         ║
║                                                               ║
║  COMMON COMMANDS:                                             ║
║  ./gradlew build → Build app                                  ║
║  ./gradlew installDebug → Install on device                   ║
║  ./gradlew signingReport → Get SHA-1 certificate              ║
║                                                               ║
╚═══════════════════════════════════════════════════════════════╝
```


