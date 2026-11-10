package com.example.mrdetective.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
) {
    val c = AppTheme.colors
    val size = modifier.fillMaxWidth().height(Sizes.controlHeight)
    val label: @Composable () -> Unit = {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
    when (style) {
        AppButtonStyle.Primary ->
            Button(
                onClick = onClick,
                modifier = size,
                enabled = enabled,
                shape = AppShapes.control,
                colors = ButtonDefaults.buttonColors(containerColor = c.button, contentColor = c.onButton),
            ) { label() }

        AppButtonStyle.Accent ->
            Button(
                onClick = onClick,
                modifier = size,
                enabled = enabled,
                shape = AppShapes.control,
                colors = ButtonDefaults.buttonColors(containerColor = c.accent, contentColor = c.onAccent),
            ) { label() }

        AppButtonStyle.Outlined ->
            OutlinedButton(
                onClick = onClick,
                modifier = size,
                enabled = enabled,
                shape = AppShapes.control,
                border = BorderStroke(1.dp, c.outline),
                colors =
                    ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = c.text,
                    ),
            ) { label() }
    }
}
