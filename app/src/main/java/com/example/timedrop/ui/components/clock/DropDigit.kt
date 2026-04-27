package com.example.timedrop.ui.components.clock

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun DropDigit(
    digit: Char,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable (Char) -> Unit,
) {
    if (!enabled) {
        content(digit)
        return
    }

    AnimatedContent(
        targetState = digit,
        transitionSpec = {
            dropDown() // new digit falls in
        },
        label = "DropDigit",
        modifier = modifier,
        contentAlignment = contentAlignment,
    ) { d ->
        content(d)
    }
}

private fun dropDown(): ContentTransform {
    val duration = 220
    return (slideInVertically(animationSpec = tween(duration)) { height -> -height / 2 } + fadeIn(
        animationSpec = tween(duration)
    )).togetherWith(
        slideOutVertically(animationSpec = tween(duration)) { height -> height / 2 } + fadeOut(
            animationSpec = tween(duration)
        )
    )
}

