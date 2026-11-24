package com.example.mrdetective.presentation.ui.components

import androidx.compose.ui.res.stringResource
import com.example.mrdetective.presentation.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.mrdetective.presentation.ui.theme.AppShapes
import com.example.mrdetective.presentation.ui.theme.AppTheme

@Composable
internal fun AppErrorDialog(message: String, onClose: () -> Unit, onRetry: (() -> Unit)? = null) {
    val colors = AppTheme.colors
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(stringResource(R.string.error_title)) },
        text = { Text(message) },
        confirmButton = {
            if (LocalDensity.current.fontScale >= 1.3f) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (onRetry != null) AppButton(stringResource(R.string.retry), onRetry, style = AppButtonStyle.Accent)
                    AppButton(stringResource(R.string.close), onClose, style = AppButtonStyle.Outlined)
                }
            } else {
                Row(Modifier.padding(horizontal = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppButton(stringResource(R.string.close), onClose, Modifier.weight(1f), style = AppButtonStyle.Outlined)
                    if (onRetry != null) AppButton(stringResource(R.string.retry), onRetry, Modifier.weight(1f), style = AppButtonStyle.Accent)
                }
            }
        },
        shape = AppShapes.card,
        containerColor = colors.surface,
        titleContentColor = colors.error,
        textContentColor = colors.text,
    )
}

@Composable
internal fun ErrorNotice(message: String?, onRetry: (() -> Unit)? = null) {
    if (message == null) return
    var open by rememberSaveable(message) { mutableStateOf(true) }
    if (open) {
        AppErrorDialog(message, { open = false }, onRetry)
    } else if (onRetry != null) {
        AppButton(stringResource(R.string.retry), onRetry, style = AppButtonStyle.Outlined)
    }
}
