package com.example.timedrop.ui.navigation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

// ── Design palette for BottomNav ──
private val Lavender = Color(0xFFA5A5FF)
private val Orchid = Color(0xFFD277FF)
private val Slate = Color(0xFFADAAAA)

data class NavItem(val route: String, val icon: ImageVector)

val navItemDefinitions = mapOf(
    TimeDropDestination.Home.route to NavItem(TimeDropDestination.Home.route, Icons.Filled.Home),
    TimeDropDestination.Pomodoro.route to NavItem(TimeDropDestination.Pomodoro.route, Icons.Filled.HourglassBottom),
    TimeDropDestination.Stopwatch.route to NavItem(TimeDropDestination.Stopwatch.route, Icons.Filled.Timer),
    TimeDropDestination.Music.route to NavItem(TimeDropDestination.Music.route, Icons.Filled.MusicNote),
    TimeDropDestination.WorldClock.route to NavItem(TimeDropDestination.WorldClock.route, Icons.Filled.Public),
    TimeDropDestination.Calendar.route to NavItem(TimeDropDestination.Calendar.route, Icons.Filled.CalendarMonth),
    TimeDropDestination.Notes.route to NavItem(TimeDropDestination.Notes.route, Icons.Filled.Description),
    TimeDropDestination.Settings.route to NavItem(TimeDropDestination.Settings.route, Icons.Filled.Settings),
)

val defaultNavOrder = listOf(
    TimeDropDestination.Home.route,
    TimeDropDestination.Pomodoro.route,
    TimeDropDestination.Stopwatch.route,
    TimeDropDestination.WorldClock.route,
    TimeDropDestination.Music.route,
    TimeDropDestination.Calendar.route,
    TimeDropDestination.Notes.route,
    TimeDropDestination.Settings.route
)

@Composable
fun ReorderableBottomBar(
    currentRoute: String?,
    navOrder: List<String>,
    onOrderChanged: (List<String>) -> Unit,
    onNavigate: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        color = Color(0xFF2C2C2C).copy(alpha = 0.40f),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Resolve actual items based on preference order (fallback to default if mismatch)
        val finalOrder = remember(navOrder) {
            val orderToUse = if (navOrder.isEmpty() || navOrder.size != defaultNavOrder.size) {
                defaultNavOrder
            } else {
                navOrder
            }
            // Safely map to NavItems
            orderToUse.mapNotNull { navItemDefinitions[it] } 
        }

        val items = remember { mutableStateListOf(*finalOrder.toTypedArray()) }
        
        // Keep synced with upstream if it changes via other device
        LaunchedEffect(finalOrder) {
            items.clear()
            items.addAll(finalOrder)
        }

        var draggedIndex by remember { mutableStateOf<Int?>(null) }
        var offsetX by remember { mutableStateOf(0f) }
        val itemWidth = 72.dp // Estimated width per item for drag threshold

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .height(64.dp), // Replaced IntrinsicSize.Max to prevent crash
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 24.dp)
        ) {
            itemsIndexed(items, key = { _, item -> item.route }) { index, item ->
                
                val isActive = currentRoute == item.route || 
                                (item.route == TimeDropDestination.Home.route && currentRoute == null)

                val offset by animateDpAsState(
                    targetValue = if (index == draggedIndex) offsetX.dp else 0.dp,
                    label = "dragOffset"
                )
                val scale by animateFloatAsState(
                    targetValue = if (index == draggedIndex) 1.2f else 1f,
                    label = "dragScale"
                )

                Box(
                    modifier = Modifier
                        .fillMaxHeight() // Now works because parent has fixed height
                        .offset { IntOffset(offset.roundToPx(), 0) }
                        .scale(scale)
                        .pointerInput(Unit) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = {
                                    draggedIndex = index
                                },
                                onDragEnd = {
                                    draggedIndex = null
                                    offsetX = 0f
                                    onOrderChanged(items.map { it.route })
                                },
                                onDragCancel = {
                                    draggedIndex = null
                                    offsetX = 0f
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    offsetX += dragAmount.x / density
                                    
                                    val currentIdx = draggedIndex ?: return@detectDragGesturesAfterLongPress
                                    // Calculate if we moved enough to swap
                                    val swapThreshold = itemWidth.value * 0.8f
                                    if (offsetX > swapThreshold && currentIdx < items.lastIndex) {
                                        // Swap Right
                                        val temp = items[currentIdx]
                                        items[currentIdx] = items[currentIdx + 1]
                                        items[currentIdx + 1] = temp
                                        draggedIndex = currentIdx + 1
                                        offsetX -= itemWidth.value
                                    } else if (offsetX < -swapThreshold && currentIdx > 0) {
                                        // Swap Left
                                        val temp = items[currentIdx]
                                        items[currentIdx] = items[currentIdx - 1]
                                        items[currentIdx - 1] = temp
                                        draggedIndex = currentIdx - 1
                                        offsetX += itemWidth.value
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isActive) {
                        Surface(
                            onClick = { },
                            shape = CircleShape,
                            color = Color.Transparent,
                            modifier = Modifier.size(56.dp),
                            shadowElevation = if (index == draggedIndex) 16.dp else 8.dp
                        ) {
                            val bgModifier = if (item.route == TimeDropDestination.Calendar.route) {
                                Modifier.fillMaxSize().background(Lavender.copy(alpha = 0.2f), CircleShape)
                            } else {
                                Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Lavender, Orchid)), CircleShape)
                            }
                            
                            Box(
                                modifier = bgModifier, 
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(item.icon, contentDescription = item.route, tint = Color.White)
                            }
                        }
                    } else {
                        IconButton(
                            onClick = { onNavigate(item.route) },
                            modifier = Modifier.padding(14.dp) // Maintain similar size box
                        ) {
                            Icon(item.icon, contentDescription = item.route, tint = Slate.copy(alpha = 0.8f), modifier = Modifier.size(28.dp))
                        }
                    }
                }
            }
        }
    }
}
