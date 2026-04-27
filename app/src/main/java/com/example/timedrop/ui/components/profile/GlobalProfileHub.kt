package com.example.timedrop.ui.components.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.timedrop.data.local.AppDatabase
import com.example.timedrop.data.local.User
import com.example.timedrop.ui.screens.profile.ProfileOverlay
import androidx.compose.ui.platform.LocalContext

@Composable
fun GlobalProfileHub(
    currentUserEmail: String,
    modifier: Modifier = Modifier
) {
    if (currentUserEmail.isBlank()) return

    var showProfile by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val userDao = AppDatabase.getDatabase(context).userDao()
    val user by userDao.getUserByEmail(currentUserEmail).collectAsState(initial = null)

    Box(
        modifier = modifier
            .padding(top = 24.dp, end = 24.dp)
            .size(42.dp)
            .clip(CircleShape)
            .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
            .background(Color(0xFF262626))
            .clickable { showProfile = true },
        contentAlignment = Alignment.Center
    ) {
        if (user?.photoUri != null) {
            AsyncImage(
                model = user?.photoUri,
                contentDescription = "Global Profile",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                tint = Color(0xFFA5A5FF).copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp)
            )
        }
    }

    if (showProfile) {
        ProfileOverlay(
            userEmail = currentUserEmail,
            onDismiss = { showProfile = false }
        )
    }
}
