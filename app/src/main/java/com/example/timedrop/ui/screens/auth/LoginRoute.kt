package com.example.timedrop.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.luminance

// Design palette is now dynamic from MaterialTheme.colorScheme in the composable

@Composable
fun LoginRoute(
    onNavigateToSignUp: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    val colors = MaterialTheme.colorScheme
    val Primary = colors.primary
    val Secondary = colors.secondary
    val onBg = colors.onBackground
    val OnSurfaceVariant = colors.onSurfaceVariant
    val surfaceContainer = colors.surface
    val surfaceHigh = colors.surfaceVariant
    val surfaceLow = colors.surface
    val OutlineVariant = colors.outline

    if (state.isSuccess) {
        onLoginSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // ── Background Accents ──
        val glowAlpha = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) 0.10f else 0.05f
        Box(
            modifier = Modifier
                .size(400.dp)
                .offset(x = (-100).dp, y = (-100).dp)
                .blur(120.dp)
                .background(Primary.copy(alpha = glowAlpha), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(320.dp) // Roughly 40%
                .align(Alignment.BottomEnd)
                .offset(x = 80.dp, y = 80.dp)
                .blur(100.dp)
                .background(Secondary.copy(alpha = 0.05f), CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ── Brand Header ──
            Surface(
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(24.dp),
                color = surfaceHigh, // surface-container-high
                border = androidx.compose.foundation.BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.1f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.HourglassEmpty,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = Primary
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "Welcome back",
                style = TextStyle(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.onBackground,
                    letterSpacing = (-0.5).sp
                )
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Continue your journey through time.",
                style = TextStyle(fontSize = 14.sp, color = OnSurfaceVariant, letterSpacing = 0.5.sp)
            )

            Spacer(Modifier.height(40.dp))

            // ── Login Form ──
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LoginTextField(
                    label = "EMAIL ADDRESS",
                    placeholder = "name@example.com",
                    value = state.email,
                    onValueChange = viewModel::onEmailChange,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    OnSurfaceVariant = OnSurfaceVariant,
                    Primary = Primary,
                    colors = colors
                )

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "PASSWORD",
                            style = TextStyle(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnSurfaceVariant,
                                letterSpacing = 2.sp
                            )
                        )
                        Text(
                            "Forgot Password?",
                            style = TextStyle(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Primary,
                                letterSpacing = 2.sp
                            ),
                            modifier = Modifier.clickable { /* TODO */ }
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    LoginTextField(
                        label = "", // Already handled above
                        placeholder = "••••••••",
                        value = state.password,
                        onValueChange = viewModel::onPasswordChange,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        OnSurfaceVariant = OnSurfaceVariant,
                        Primary = Primary,
                        colors = colors
                    )
                }

                if (state.error != null) {
                    Text(
                        state.error!!,
                        color = colors.error,
                        style = TextStyle(fontSize = 12.sp),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Action Button
                Button(
                    onClick = viewModel::signIn,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(brush = Brush.horizontalGradient(listOf(Primary, Secondary))),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Sign In",
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.onPrimary
                                )
                            )
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = colors.onPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(40.dp))

            // ── Social Logins ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = OutlineVariant)
                Text(
                    "OR CONTINUE WITH",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = TextStyle(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceVariant,
                        letterSpacing = 2.sp
                    )
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = OutlineVariant)
            }

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SocialLoginButton(
                    iconRaw = { AppleIcon(Modifier.size(20.dp), color = colors.onSurface) },
                    label = "Apple",
                    modifier = Modifier.weight(1f),
                    surfaceContainer = surfaceContainer,
                    OutlineVariant = OutlineVariant,
                    onSurface = colors.onSurface
                )
                SocialLoginButton(
                    iconRaw = { GoogleIcon(Modifier.size(20.dp)) },
                    label = "Google",
                    modifier = Modifier.weight(1f),
                    surfaceContainer = surfaceContainer,
                    OutlineVariant = OutlineVariant,
                    onSurface = colors.onSurface
                )
            }

            Spacer(Modifier.height(40.dp))

            // ── Footer ──
            Row(
                modifier = Modifier.padding(bottom = 32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "New to TimeDrop?",
                    style = TextStyle(fontSize = 14.sp, color = OnSurfaceVariant)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    "Create an account",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    ),
                    modifier = Modifier.clickable { onNavigateToSignUp() }
                )
            }
        }
    }
}

@Composable
private fun LoginTextField(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    OnSurfaceVariant: Color,
    Primary: Color,
    colors: androidx.compose.material3.ColorScheme
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (label.isNotEmpty()) {
            Text(
                label,
                style = TextStyle(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceVariant,
                    letterSpacing = 2.sp
                ),
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )
        }
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = OnSurfaceVariant) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = colors.surfaceVariant,
                unfocusedContainerColor = colors.surfaceVariant,
                focusedIndicatorColor = Primary,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = colors.onSurface,
                unfocusedTextColor = colors.onSurface,
                cursorColor = Primary
            ),
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions
        )
    }
}

@Composable
private fun SocialLoginButton(
    iconRaw: @Composable () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    surfaceContainer: Color,
    OutlineVariant: Color,
    onSurface: Color
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(54.dp),
        shape = RoundedCornerShape(12.dp),
        color = surfaceContainer,
        border = androidx.compose.foundation.BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            iconRaw()
            Spacer(Modifier.width(12.dp))
            Text(
                label,
                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White)
            )
        }
    }
}

@Composable
private fun AppleIcon(modifier: Modifier = Modifier, color: Color = Color.White) {
    Icon(Icons.Filled.Settings, contentDescription = null, modifier = modifier, tint = color)
}

@Composable
private fun GoogleIcon(modifier: Modifier = Modifier) {
    // Simple mock Google G
    Icon(Icons.Filled.HourglassEmpty, contentDescription = null, modifier = modifier, tint = Color.Red)
}

