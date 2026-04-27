package com.example.timedrop.ui.components.clock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.BoxScope
import kotlin.math.max
import kotlin.math.min

@Composable
fun FlipSegment(
    text: String,
    animateDigits: Boolean,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    contentPadding: PaddingValues = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
    fontSizeSp: Int = 64,
    topLabel: String? = null,
    bottomLeftLabel: String? = null,
    bottomRightLabel: String? = null,
) {
    val shape = RoundedCornerShape(18.dp)

    BoxWithConstraints(
        modifier = modifier
            .clip(shape)
            .background(containerColor)
            .drawWithContent {
                drawContent()
                // subtle center divider like flip clocks
                val y = size.height / 2f
                drawLine(
                    color = Color.Black.copy(alpha = 0.10f),
                    start = androidx.compose.ui.geometry.Offset(0f, y),
                    end = androidx.compose.ui.geometry.Offset(size.width, y),
                    strokeWidth = 1.5f,
                )
            }
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        val reserveTop = if (topLabel != null) 28.dp else 0.dp
        val reserveBottom = if (bottomLeftLabel != null || bottomRightLabel != null) 26.dp else 0.dp
        val availableHeight = (maxHeight - reserveTop - reserveBottom).value
        val fittedFontSize = min(fontSizeSp, max(28, (availableHeight * 0.62f).toInt()))

        DigitRow(
            text = text,
            animateDigits = animateDigits,
            color = contentColor,
            fontSizeSp = fittedFontSize,
            modifier = Modifier.padding(top = reserveTop, bottom = reserveBottom),
        )

        if (topLabel != null) {
            Text(
                text = topLabel,
                style = MaterialTheme.typography.titleSmall,
                color = contentColor.copy(alpha = 0.85f),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 10.dp),
                textAlign = TextAlign.Center,
            )
        }
        if (bottomLeftLabel != null) {
            Text(
                text = bottomLeftLabel,
                style = MaterialTheme.typography.titleSmall,
                color = contentColor.copy(alpha = 0.85f),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 12.dp, bottom = 10.dp),
            )
        }
        if (bottomRightLabel != null) {
            Text(
                text = bottomRightLabel,
                style = MaterialTheme.typography.titleSmall,
                color = contentColor.copy(alpha = 0.85f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 12.dp, bottom = 10.dp),
            )
        }
    }
}

@Composable
private fun DigitRow(
    text: String,
    animateDigits: Boolean,
    color: Color,
    fontSizeSp: Int,
    modifier: Modifier = Modifier,
) {
    // Keep it simple: render as a row of per-char animated digits.
    // Only digits animate; separators render as-is.
    androidx.compose.foundation.layout.Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
    ) {
        val style = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = fontSizeSp.sp,
            letterSpacing = (-0.5).sp,
            color = color,
            textAlign = TextAlign.Center,
        )
        text.forEach { ch ->
            if (ch.isDigit()) {
                DropDigit(digit = ch, enabled = animateDigits) { d ->
                    Text(text = d.toString(), style = style)
                }
            } else {
                Text(text = ch.toString(), style = style)
            }
        }
    }
}

