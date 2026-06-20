package com.example.timedrop.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

// Design palette
private val Lavender = Color(0xFFA5A5FF)
private val Orchid = Color(0xFFD277FF)
private val DeepBg = Color(0xFF0E0E0E)
private val SurfaceContainer = Color(0xFF1A1A1A)
private val Primary = Color(0xFFA5A5FF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileOverlay(
    userEmail: String,
    onDismiss: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var isEditing by remember { mutableStateOf(false) }
    var editedAlias by remember { mutableStateOf("") }
    var editedFullName by remember { mutableStateOf("") }
    
    // Initialize edited fields when user is loaded
    LaunchedEffect(uiState.user) {
        uiState.user?.let {
            editedAlias = it.alias ?: ""
            editedFullName = it.fullName
        }
    }

    LaunchedEffect(userEmail) {
        viewModel.loadUser(userEmail)
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.updatePhoto(it.toString()) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DeepBg,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.2f)) },
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with Edit/Save button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isEditing) {
                    TextButton(onClick = { isEditing = false }) {
                        Text("Cancel", color = Color.White.copy(alpha = 0.5f))
                    }
                } else {
                    Spacer(Modifier.width(64.dp))
                }
                
                Text(
                    "Profile Identity",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                TextButton(
                    onClick = {
                        if (isEditing) {
                            viewModel.updateProfile(editedFullName, editedAlias)
                            isEditing = false
                        } else {
                            isEditing = true
                        }
                    }
                ) {
                    Text(
                        if (isEditing) "Save" else "Edit",
                        color = Lavender,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(Modifier.height(32.dp))
            
            // Profile Picture
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainer)
                        .border(2.dp, Brush.linearGradient(listOf(Lavender, Orchid)), CircleShape)
                        .clickable { photoPickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.user?.photoUri != null) {
                        AsyncImage(
                            model = uiState.user?.photoUri,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Filled.Person,
                            null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.White.copy(alpha = 0.2f)
                        )
                    }
                }
                
                Surface(
                    onClick = { photoPickerLauncher.launch("image/*") },
                    shape = CircleShape,
                    color = Lavender,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (uiState.user?.photoUri == null) Icons.Filled.AddAPhoto else Icons.Filled.Edit,
                            null, 
                            tint = DeepBg, 
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(40.dp))
            
            uiState.user?.let { user ->
                ProfileField(
                    label = "Alias", 
                    value = if (isEditing) editedAlias else (user.alias ?: "Not set"), 
                    icon = Icons.Filled.AlternateEmail,
                    isEditing = isEditing,
                    onValueChange = { editedAlias = it }
                )
                Spacer(Modifier.height(16.dp))
                
                ProfileField(
                    label = "Full Name", 
                    value = if (isEditing) editedFullName else user.fullName, 
                    icon = Icons.Filled.Badge,
                    isEditing = isEditing,
                    onValueChange = { editedFullName = it }
                )
                Spacer(Modifier.height(16.dp))
                
                ProfileField(
                    label = "Email", 
                    value = user.email, 
                    icon = Icons.Filled.Email,
                    isEditing = false // Email usually stays the same
                )
                Spacer(Modifier.height(16.dp))
                
                var passwordVisible by remember { mutableStateOf(false) }
                ProfileField(
                    label = "Password", 
                    value = user.password, 
                    icon = Icons.Filled.Lock,
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    onTogglePassword = { passwordVisible = !passwordVisible },
                    isEditing = false
                )
                
                Spacer(Modifier.height(24.dp))
                Divider(color = Color.White.copy(alpha = 0.05f))
                Spacer(Modifier.height(24.dp))
                
                // --- CLOUD IDENTITY ---
                val firebaseUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "Not Connected"
                ProfileField(
                    label = "Cloud Identity (Sync ID)", 
                    value = firebaseUid, 
                    icon = Icons.Filled.CloudSync,
                    isEditing = false,
                    textColor = Lavender
                )
            }
            
            Spacer(Modifier.height(40.dp))
            
            if (!isEditing) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceContainer),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Text("Close", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun ProfileField(
    label: String,
    value: String,
    icon: ImageVector,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: (() -> Unit)? = null,
    isEditing: Boolean = false,
    onValueChange: (String) -> Unit = {},
    textColor: Color = Color.White
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            label,
            style = TextStyle(fontSize = 12.sp, color = Color.White.copy(alpha = 0.4f), fontWeight = FontWeight.Medium)
        )
        Spacer(Modifier.height(8.dp))
        
        if (isEditing) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceContainer),
                leadingIcon = { Icon(icon, null, tint = Lavender, modifier = Modifier.size(20.dp)) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = SurfaceContainer,
                    unfocusedContainerColor = SurfaceContainer,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Lavender
                ),
                textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                singleLine = true
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceContainer, RoundedCornerShape(12.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, null, tint = Lavender, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(16.dp))
                Text(
                    text = if (isPassword && !passwordVisible) "••••••••" else value,
                    style = TextStyle(fontSize = 14.sp, color = textColor, fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.weight(1f)
                )
                if (isPassword && onTogglePassword != null) {
                    IconButton(onClick = onTogglePassword) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
