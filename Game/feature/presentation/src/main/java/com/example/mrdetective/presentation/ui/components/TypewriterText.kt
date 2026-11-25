package com.example.mrdetective.presentation.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import kotlinx.coroutines.delay

/** Показывает текст постепенно, сохраняя место под весь текст. */
@Composable
fun TypewriterText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    animate: Boolean = true,
) {
    var visibleEnd by rememberSaveable(text) { mutableIntStateOf(if (animate) 0 else text.length) }
    LaunchedEffect(text, animate) {
        if (!animate) {
            visibleEnd = text.length
        } else {
            val charactersPerTick = (text.codePointCount(0, text.length) / 120).coerceAtLeast(1)
            while (visibleEnd < text.length) {
                delay(22)
                val remaining = text.codePointCount(visibleEnd, text.length)
                visibleEnd = text.offsetByCodePoints(visibleEnd, charactersPerTick.coerceAtMost(remaining))
            }
        }
    }

    val displayed =
        buildAnnotatedString {
            append(text)
            if (animate && visibleEnd < text.length) {
                addStyle(SpanStyle(color = Color.Transparent), visibleEnd, text.length)
            }
        }
    Text(displayed, modifier = modifier, style = style)
}
