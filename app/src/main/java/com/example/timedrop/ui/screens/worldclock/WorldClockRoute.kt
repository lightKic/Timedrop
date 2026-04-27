package com.example.timedrop.ui.screens.worldclock

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import androidx.compose.ui.res.painterResource
import com.example.timedrop.R
import java.time.format.DateTimeFormatter
import java.util.Locale

// Design palette is now dynamic from MaterialTheme.colorScheme in the composable

@Composable
fun WorldClockRoute(
    onBack: () -> Unit,
    vm: WorldClockViewModel = viewModel()
) {
    val uiState by vm.uiState.collectAsState()

    val colors = MaterialTheme.colorScheme
    val Lavender = colors.primary
    val Orchid = colors.secondary
    val onBg = colors.onBackground
    val Slate = colors.onSurfaceVariant
    val surfaceContainer = colors.surfaceContainer
    val surfaceHigh = colors.surfaceVariant
    val mapOverlay = colors.surface

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { vm.openAddCityDialog() },
                shape = CircleShape,
                containerColor = Color.Transparent,
                modifier = Modifier
                    .padding(bottom = 80.dp)
                    .background(Brush.linearGradient(listOf(Lavender, Orchid)), CircleShape)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add City", tint = Color.White)
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { padding ->

        @Composable
        fun LeftSide() {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Slate)
                    }
                    Spacer(Modifier.weight(1f))
                    Text("TimeDrop", style = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, brush = Brush.linearGradient(listOf(Lavender, Orchid))))
                    Spacer(Modifier.weight(1f))
                    // Search and More icons removed
                    Spacer(Modifier.size(48.dp))
                }

                Column(modifier = Modifier.padding(horizontal = 28.dp, vertical = 12.dp)) {
                    Text("World Clock", style = TextStyle(fontSize = 36.sp, fontWeight = FontWeight.Bold, color = colors.onBackground, letterSpacing = (-1).sp))
                    Spacer(Modifier.height(8.dp))
                    Text("GLOBAL SYNCHRONIZATION", style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.5.sp, color = Slate))
                }

                Spacer(Modifier.height(16.dp))

                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                    Surface(shape = RoundedCornerShape(32.dp), color = surfaceHigh, modifier = Modifier.fillMaxWidth()) {
                        val localTime = uiState.currentLocalTime
                        val timeFormatter = DateTimeFormatter.ofPattern("hh:mm", Locale.getDefault())
                        val amPmFormatter = DateTimeFormatter.ofPattern("a", Locale.getDefault())
                        val isDaytime = localTime.hour in 6..18

                        Box {
                            Box(Modifier.size(180.dp).align(Alignment.BottomEnd).offset(x = 48.dp, y = 48.dp).blur(80.dp).background(Lavender.copy(alpha = 0.15f), CircleShape))
                            Column(modifier = Modifier.fillMaxWidth().padding(32.dp)) {
                                Text("CURRENT LOCATION", style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, color = Lavender), modifier = Modifier.align(Alignment.End))
                                Spacer(Modifier.height(16.dp))
                                Text("Local time", style = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, color = colors.onSurfaceVariant))
                                Text("Today", style = TextStyle(fontSize = 14.sp, color = Slate))
                                Spacer(Modifier.height(24.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text(localTime.format(timeFormatter), style = TextStyle(fontSize = 64.sp, fontWeight = FontWeight.ExtraBold, color = colors.onSurfaceVariant, letterSpacing = (-2).sp))
                                        Text(localTime.format(amPmFormatter), style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Medium, color = Slate), modifier = Modifier.padding(bottom = 12.dp, start = 8.dp))
                                    }
                                    Box(modifier = Modifier.size(48.dp).background((if (isDaytime) Color.Yellow else Lavender).copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                                        Icon(if (isDaytime) Icons.Filled.LightMode else Icons.Filled.DarkMode, null, tint = if (isDaytime) Color.Yellow else Lavender)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).aspectRatio(16f / 9f).clip(RoundedCornerShape(32.dp)).background(surfaceHigh)) {
                    androidx.compose.foundation.Image(
                        painter = painterResource(R.drawable.world_map_premium),
                        contentDescription = "World Map",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(Modifier.fillMaxSize()) {
                        uiState.savedCities.forEach { cityState ->
                            val city = cityState.city
                            
                            // 1. Horizontal Mapping (Longitude is linear)
                            // Calibrated shift to move America more to the left
                            val horizontalBias = (city.longitude / 190).toFloat() - 0.06f

                            // 2. Vertical Mapping (Mercator Projection)
                            val latRad = Math.toRadians(city.latitude)
                            val mercN = Math.log(Math.tan((Math.PI / 4) + (latRad / 2)))
                            // Scale mercN (~3.1 max) to bias range, with offset
                            val verticalBias = -(mercN / 2.2).toFloat() + 0.15f

                            PulseDot(
                                lavender = Lavender,
                                modifier = Modifier.align(BiasAlignment(horizontalBias.coerceIn(-1f, 1f), verticalBias.coerceIn(-1f, 1f)))
                            )
                        }
                    }
                    Box(modifier = Modifier.align(Alignment.BottomStart).padding(24.dp).background(mapOverlay.copy(alpha = 0.8f), RoundedCornerShape(999.dp)).padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(8.dp).background(Lavender, CircleShape))
                            Spacer(Modifier.width(8.dp))
                            Text("LIVE GLOBAL TRACKING", style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, color = colors.onSurface))
                        }
                    }
                }
            }
        }

        @Composable
        fun RightSide() {
            Column {
                Spacer(Modifier.height(16.dp))
                Column(modifier = Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    uiState.savedCities.forEach { cityState ->
                        Surface(onClick = { }, shape = RoundedCornerShape(24.dp), color = surfaceContainer, modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text(cityState.dayStatus.uppercase(), style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, color = Slate))
                                    Spacer(Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(cityState.city.name, style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = colors.onSurface))
                                        Spacer(Modifier.width(8.dp))
                                        Icon(if (cityState.isDaytime) Icons.Filled.LightMode else Icons.Filled.DarkMode, null, tint = if (cityState.isDaytime) Color.Yellow else Lavender, modifier = Modifier.size(16.dp))
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Row(verticalAlignment = Alignment.Bottom) {
                                            Text(cityState.timeStr, style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White))
                                            Text(cityState.amPm, style = TextStyle(fontSize = 12.sp, color = Slate), modifier = Modifier.padding(bottom = 3.dp, start = 4.dp))
                                        }
                                        Text(cityState.offsetStr, style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Medium, color = Slate))
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    IconButton(onClick = { vm.removeCity(cityState.city) }) { Icon(Icons.Filled.Close, null, tint = Slate) }
                                }
                            }
                        }
                    }
                }
            }
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(padding)) {
            val landscape = maxWidth > maxHeight
            if (landscape) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.weight(1.2f).fillMaxHeight().statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState())) {
                        LeftSide()
                        Spacer(Modifier.height(100.dp))
                    }
                    Column(modifier = Modifier.weight(1f).fillMaxHeight().statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState())) {
                        RightSide()
                        Spacer(Modifier.height(100.dp))
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState())) {
                    LeftSide()
                    RightSide()
                    Spacer(Modifier.height(100.dp))
                }
            }
        }
    }

    if (uiState.isAddCityDialogOpen) {
        AlertDialog(
            onDismissRequest = { vm.closeAddCityDialog() },
            title = { Text("Select City", color = Color.White) },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                    if (uiState.availableCities.isEmpty()) {
                        item { Text("All available cities added.", color = Slate, modifier = Modifier.padding(16.dp)) }
                    } else {
                        items(uiState.availableCities) { city ->
                            Row(modifier = Modifier.fillMaxWidth().clickable { vm.addCity(city) }.padding(vertical = 12.dp, horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(city.name, color = Color.White, fontSize = 16.sp)
                                Text(city.zoneIdStr, color = Slate, fontSize = 12.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { vm.closeAddCityDialog() }) { Text("Cancel", color = Lavender) } },
            containerColor = surfaceContainer,
            titleContentColor = Color.White
        )
    }
}

@Composable
fun PulseDot(lavender: Color, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(initialValue = 1f, targetValue = 2.5f, animationSpec = infiniteRepeatable(animation = tween(1500, easing = LinearEasing), repeatMode = RepeatMode.Restart))
    val alpha by infiniteTransition.animateFloat(initialValue = 0.6f, targetValue = 0f, animationSpec = infiniteRepeatable(animation = tween(1500, easing = LinearEasing), repeatMode = RepeatMode.Restart))
    Box(modifier, contentAlignment = Alignment.Center) {
        Box(Modifier.size(6.dp).background(lavender, CircleShape))
        Box(Modifier.size(6.dp * scale).background(lavender.copy(alpha = alpha), CircleShape))
    }
}
