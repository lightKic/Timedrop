package com.example.timedrop.ui.screens.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.timedrop.data.local.Note
import java.util.*

// Design palette is now dynamic from MaterialTheme.colorScheme in the composable

@Composable
fun NotesRoute(
    viewModel: NotesViewModel,
    onNavigateToEditor: (Int) -> Unit
) {
    val notes by viewModel.allNotes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val colors = MaterialTheme.colorScheme
    val Primary = colors.primary
    val Secondary = colors.secondary
    val Slate = colors.onSurfaceVariant
    val onBg = colors.onBackground
    val surfaceContainer = colors.surface
    val surfaceHigh = colors.surfaceVariant
    val surfaceLow = colors.surface

    Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
        // Ambient Decor
        Box(
            modifier = Modifier
                .size(400.dp)
                .align(Alignment.TopEnd)
                .offset(x = 100.dp, y = (-50).dp)
                .blur(120.dp)
                .background(Primary.copy(alpha = 0.05f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-50).dp, y = 50.dp)
                .blur(100.dp)
                .background(Secondary.copy(alpha = 0.05f), CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Menu, null, tint = Primary, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(16.dp))
                    Text("Notes", style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = onBg))
                }
                Box(Modifier.size(32.dp).clip(CircleShape).border(1.dp, Slate.copy(alpha = 0.2f), CircleShape)) {
                    // Placeholder for user image
                    Box(Modifier.fillMaxSize().background(surfaceContainer))
                }
            }

            // Search
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                placeholder = { Text("Search your sanctuary...", color = Slate.copy(alpha = 0.5f)) },
                leadingIcon = { Icon(Icons.Filled.Search, null, tint = Slate) },
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = surfaceLow,
                    focusedContainerColor = surfaceLow,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Primary,
                    cursorColor = Primary
                )
            )

            // List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                items(notes) { note ->
                    NoteCard(
                        note = note, 
                        onClick = { onNavigateToEditor(note.id) },
                        onDelete = { viewModel.deleteNote(note) },
                        surfaceHigh = surfaceHigh,
                        primary = Primary,
                        slate = Slate
                    )
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = { onNavigateToEditor(0) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 100.dp, end = 24.dp)
                .size(64.dp),
            shape = CircleShape,
            containerColor = Color.Transparent,
            elevation = FloatingActionButtonDefaults.elevation(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(listOf(Primary, Secondary)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Add, null, tint = Color.White, modifier = Modifier.size(32.dp))
            }
        }
    }
}

@Composable
private fun NoteCard(note: Note, onClick: () -> Unit, onDelete: () -> Unit, surfaceHigh: Color, primary: Color, slate: Color) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = surfaceHigh,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    note.category.uppercase(), 
                    style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, color = primary)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Filled.DeleteOutline, null, tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Filled.NorthEast, null, tint = slate.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(note.title, style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White))
            Spacer(Modifier.height(8.dp))
            Text(
                note.content, 
                style = TextStyle(fontSize = 14.sp, color = slate, lineHeight = 20.sp),
                maxLines = 3
            )
            Spacer(Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(note.date, style = TextStyle(fontSize = 12.sp, color = slate.copy(alpha = 0.6f)))
                if (note.content.length > 100) {
                    Icon(Icons.Filled.Attachment, null, tint = primary, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}
