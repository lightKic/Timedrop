package com.example.timedrop.ui.screens.auth

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
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
import androidx.compose.ui.graphics.luminance

// ── Design palette (from VOI mockup) ──
// Design palette is now dynamic from MaterialTheme.colorScheme in the composable

@Composable
fun SignUpRoute(
    onNavigateToLogin: () -> Unit,
    onSignUpSuccess: () -> Unit,
    viewModel: SignUpViewModel = viewModel()
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
        onSignUpSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // ── Visual Embellishment (Background Aura) ──
        val glowAlpha = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) 0.05f else 0.02f
        Box(
            modifier = Modifier
                .size(500.dp)
                .offset(x = 200.dp, y = (-50).dp)
                .blur(120.dp)
                .background(Primary.copy(alpha = glowAlpha), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(400.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-50).dp, y = 50.dp)
                .blur(100.dp)
                .background(Secondary.copy(alpha = 0.05f), CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ── TopAppBar Semantic Shell ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.BlurOn,
                        contentDescription = null,
                        tint = Color(0xFF818CF8),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "VOI",
                        style = TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.04).sp,
                            color = onBg
                        )
                    )
                }
                Text(
                    "Help",
                    style = TextStyle(fontSize = 14.sp, color = OnSurfaceVariant),
                    modifier = Modifier.padding(8.dp)
                )
            }

            // ── Hero Heading Section ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, bottom = 48.dp, start = 24.dp, end = 24.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    "Join the\nSanctuary",
                    style = TextStyle(
                        fontSize = 48.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        lineHeight = 52.sp,
                        letterSpacing = (-1).sp
                    )
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "Step into a world of focus and tranquility.",
                    style = TextStyle(fontSize = 18.sp, color = OnSurfaceVariant, lineHeight = 26.sp)
                )
            }

            // ── Form Section ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                VoiTextField(
                    label = "FULL NAME",
                    placeholder = "Elias Vance",
                    value = state.fullName,
                    onValueChange = viewModel::onFullNameChange,
                    primary = Primary,
                    onSurfaceVariant = OnSurfaceVariant,
                    outlineVariant = OutlineVariant
                )

                VoiTextField(
                    label = "EMAIL ADDRESS",
                    placeholder = "elias@sanctuary.com",
                    value = state.email,
                    onValueChange = viewModel::onEmailChange,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    primary = Primary,
                    onSurfaceVariant = OnSurfaceVariant,
                    outlineVariant = OutlineVariant
                )

                VoiTextField(
                    label = "PASSWORD",
                    placeholder = "••••••••",
                    value = state.password,
                    onValueChange = viewModel::onPasswordChange,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    primary = Primary,
                    onSurfaceVariant = OnSurfaceVariant,
                    outlineVariant = OutlineVariant
                )

                // Terms
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Checkbox(
                        checked = state.acceptTerms,
                        onCheckedChange = viewModel::onAcceptTermsChange,
                        colors = CheckboxDefaults.colors(
                            checkedColor = Primary,
                            uncheckedColor = OutlineVariant,
                            checkmarkColor = Color.Black
                        )
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "I agree to the Terms of Service",
                        style = TextStyle(fontSize = 14.sp, color = OnSurfaceVariant)
                    )
                }

                if (state.error != null) {
                    Text(
                        state.error!!,
                        color = Color.Red,
                        style = TextStyle(fontSize = 12.sp),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                // CTA Button
                Button(
                    onClick = viewModel::signUp,
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
                            .background(
                                brush = Brush.horizontalGradient(listOf(Primary, Secondary)),
                                shape = RoundedCornerShape(28.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Create Account",
                            style = TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        )
                    }
                }

                // Divider
                Row(
                    modifier = Modifier.padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = surfaceHigh
                    )
                    Text(
                        "OR SANCTUARY VIA",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        style = TextStyle(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF666666),
                            letterSpacing = 1.sp
                        )
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = surfaceHigh
                    )
                }

                // Social Links
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SocialButton(
                        icon = Icons.Filled.BlurOn, // Mocking Google Cloud icon for now
                        label = "Google",
                        modifier = Modifier.weight(1f),
                        onSurfaceVariant = OnSurfaceVariant
                    )
                    SocialButton(
                        icon = Icons.Filled.Settings,
                        label = "Apple",
                        modifier = Modifier.weight(1f),
                        onSurfaceVariant = OnSurfaceVariant
                    )
                }

                // Footer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Already part of the VOI?",
                        style = TextStyle(fontSize = 14.sp, color = OnSurfaceVariant)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Sign in",
                        modifier = Modifier.clickable { onNavigateToLogin() },
                        style = TextStyle(
                            fontSize = 14.sp, 
                            fontWeight = FontWeight.Bold, 
                            color = Primary
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun VoiTextField(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    primary: Color,
    onSurfaceVariant: Color,
    outlineVariant: Color
) {
    var isFocused by remember { mutableStateOf(false) }
    val underlineWidth by animateDpAsState(if (isFocused) 1.dp else 1.dp) // Mocking height here for simplicity, actually it's a width expansion in HTML
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = TextStyle(
                fontSize = 11.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.SemiBold,
                color = onSurfaceVariant
            ),
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = {
                    Text(
                        placeholder,
                        color = Color(0xFF444444),
                        style = TextStyle(fontSize = 16.sp)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { isFocused = it.isFocused },
                visualTransformation = visualTransformation,
                keyboardOptions = keyboardOptions,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = primary,
                    unfocusedIndicatorColor = outlineVariant,
                    cursorColor = primary,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                textStyle = TextStyle(fontSize = 16.sp)
            )
        }
    }
}

@Composable
private fun SocialButton(
    icon: ImageVector,
    label: String,
    onSurfaceVariant: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = onSurfaceVariant, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                label,
                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White)
            )
        }
    }
}

