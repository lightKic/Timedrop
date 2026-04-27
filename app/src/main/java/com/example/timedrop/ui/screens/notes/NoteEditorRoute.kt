package com.example.timedrop.ui.screens.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Design palette is now dynamic from MaterialTheme.colorScheme in the composable

@Composable
fun NoteEditorRoute(
    noteId: Int,
    viewModel: NotesViewModel,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Ideas") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(noteId) {
        if (noteId != 0) {
            val note = viewModel.getNoteById(noteId)
            note?.let {
                title = it.title
                content = it.content
                category = it.category
            }
        }
    }

    val colors = MaterialTheme.colorScheme
    val Primary = colors.primary
    val Secondary = colors.secondary
    val Slate = colors.onSurfaceVariant
    val onBg = colors.onBackground
    val surfaceContainer = colors.surface
    val surfaceHigh = colors.surfaceVariant
    val surfaceLow = colors.surface

    Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
        // Atmosphere
        Box(
            modifier = Modifier
                .size(400.dp)
                .align(Alignment.TopEnd)
                .offset(x = 100.dp, y = (-50).dp)
                .blur(120.dp)
                .background(Primary.copy(alpha = 0.05f), CircleShape)
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(40.dp).background(surfaceContainer, CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBackIos, null, tint = Slate, modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.width(16.dp))
                    Text("Notes", style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White))
                }
                
                Button(
                    onClick = { 
                        viewModel.saveNote(noteId, title, content, category)
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.height(40.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .background(Brush.linearGradient(listOf(Primary, Secondary)), RoundedCornerShape(20.dp))
                            .padding(horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Done", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(Modifier.height(32.dp))
                
                // Date & Category
                Text(
                    SimpleDateFormat("MMMM dd, yyyy", Locale.US).format(Date()).uppercase(),
                    style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, color = Primary.copy(alpha = 0.6f))
                )
                
                // Title Input
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("The Architecture of Time", color = Color.White.copy(alpha = 0.3f)) },
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    textStyle = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, letterSpacing = (-1).sp),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Primary
                    )
                )

                // Content Input
                TextField(
                    value = content,
                    onValueChange = { content = it },
                    placeholder = { Text("Start your narrative here...", color = Color.White.copy(alpha = 0.3f)) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 400.dp),
                    textStyle = TextStyle(fontSize = 18.sp, color = Slate, lineHeight = 28.sp),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White.copy(alpha = 0.8f),
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Primary
                    )
                )

                Spacer(Modifier.height(40.dp))

                // Metadata Card (Mock)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    MetadataCard(
                        title = "METADATA",
                        content = {
                            Column {
                                MetadataRow("Word Count", "${content.split("\\s+".toRegex()).size} words", Slate, onBg)
                                MetadataRow("Reading Time", "${maxOf(1, content.length / 500)} min", Slate, onBg)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        surfaceLow = surfaceLow,
                        slate = Slate
                    )
                }
                
                Spacer(Modifier.height(140.dp))
            }
        }

    }
}

@Composable
private fun MetadataCard(title: String, content: @Composable () -> Unit, modifier: Modifier = Modifier, surfaceLow: Color, slate: Color) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = surfaceLow,
        border = androidx.compose.foundation.BorderStroke(1.dp, slate.copy(alpha = 0.1f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, color = slate.copy(alpha = 0.6f)))
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun MetadataRow(label: String, value: String, slate: Color, onBg: Color) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = TextStyle(fontSize = 12.sp, color = slate))
        Text(value, style = TextStyle(fontSize = 12.sp, color = onBg))
    }
}
