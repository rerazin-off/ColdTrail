package com.example.mrdetective.presentation.ui.components

import androidx.compose.ui.res.stringResource
import com.example.mrdetective.presentation.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.example.mrdetective.presentation.ui.theme.AppTheme
import kotlinx.coroutines.delay

@Composable
internal fun ClueRewardDialog(
    title: String?,
    description: String?,
    onContinue: () -> Unit,
) {
    val colors = AppTheme.colors
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val continueToCase by rememberUpdatedState(onContinue)
    val readTime = ((description?.length ?: 0) * 55L).coerceIn(6_000L, 18_000L)
    LaunchedEffect(lifecycle, readTime) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            delay(readTime)
            continueToCase()
        }
    }

    AlertDialog(
        onDismissRequest = onContinue,
        title = { Text(stringResource(R.string.clue_found)) },
        text = {
            Column(
                Modifier.heightIn(max = 240.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                title?.let { Text(it, style = MaterialTheme.typography.titleMedium) }
                Text(description?.takeIf { it.isNotBlank() } ?: stringResource(R.string.clue_added))
            }
        },
        confirmButton = { AppButton(stringResource(R.string.back_to_case), onContinue, style = AppButtonStyle.Accent) },
        containerColor = colors.surface,
        titleContentColor = colors.text,
        textContentColor = colors.text,
    )
}
