package com.example.mrdetective.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mrdetective.presentation.ui.theme.AppShapes
import com.example.mrdetective.presentation.ui.theme.AppTheme
import com.example.mrdetective.presentation.ui.theme.Sizes

enum class AppButtonStyle { Primary, Accent, Outlined }


@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: AppButtonStyle = AppButtonStyle.Primary,
    enabled: Boolean = true,
    compact: Boolean = false,
) {
    val c = AppTheme.colors
    val size = modifier.fillMaxWidth().heightIn(min = Sizes.controlHeight)
    val padding = if (compact) PaddingValues(horizontal = 4.dp, vertical = 4.dp) else ButtonDefaults.ContentPadding
    val label: @Composable () -> Unit = {
        Text(
            text = text,
            style = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
        )
    }
    when (style) {
        AppButtonStyle.Primary ->
            Button(
                onClick = onClick,
                modifier = size,
                enabled = enabled,
                shape = AppShapes.control,
                contentPadding = padding,
                colors = ButtonDefaults.buttonColors(containerColor = c.button, contentColor = c.onButton),
            ) { label() }

        AppButtonStyle.Accent ->
            Button(
                onClick = onClick,
                modifier = size,
                enabled = enabled,
                shape = AppShapes.control,
                contentPadding = padding,
                colors = ButtonDefaults.buttonColors(containerColor = c.accent, contentColor = c.onAccent),
            ) { label() }

        AppButtonStyle.Outlined ->
            OutlinedButton(
                onClick = onClick,
                modifier = size,
                enabled = enabled,
                shape = AppShapes.control,
                contentPadding = padding,
                border = BorderStroke(1.dp, c.outline),
                colors =
                    ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = c.text,
                    ),
            ) { label() }
    }
}
