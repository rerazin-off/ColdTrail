package com.example.mrdetective.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.mrdetective.presentation.R
import com.example.mrdetective.presentation.ui.theme.AppShapes
import com.example.mrdetective.presentation.ui.theme.AppTheme

@Composable
internal fun NumericPuzzleSlots(
    count: Int,
    answer: String,
    solved: Boolean,
    invalid: Boolean,
) {
    val colors = AppTheme.colors
    val border =
        when {
            solved -> colors.success
            invalid -> colors.error
            else -> colors.outline
        }
    (0 until count).toList().chunked(6).forEach { slots ->
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            slots.forEach { index ->
                Surface(
                    modifier = Modifier.weight(1f).heightIn(min = 58.dp),
                    shape = AppShapes.control,
                    color = if (solved) colors.success else colors.surface2,
                    contentColor = if (solved) colors.bg else colors.text,
                    border = BorderStroke(1.dp, border),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(answer.getOrNull(index)?.toString() ?: "–", style = MaterialTheme.typography.headlineMedium)
                    }
                }
            }
        }
    }
}

@Composable
internal fun NumericPuzzleKeypad(
    onDigit: (String) -> Unit,
    onDelete: () -> Unit,
    onSubmit: () -> Unit,
    enabled: Boolean,
    canSubmit: Boolean = enabled,
) {
    val deleteLabel = stringResource(R.string.delete_digit)
    val submitLabel = stringResource(R.string.check_code)
    Column(Modifier.padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "⌫", "0", "✓").chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { key ->
                    val keyModifier = Modifier.weight(1f)
                    when (key) {
                        "⌫" ->
                            AppButton(
                                key,
                                onDelete,
                                keyModifier.semantics { contentDescription = deleteLabel },
                                style = AppButtonStyle.Outlined,
                                enabled = enabled,
                            )
                        "✓" ->
                            AppButton(
                                key,
                                onSubmit,
                                keyModifier.semantics { contentDescription = submitLabel },
                                style = AppButtonStyle.Primary,
                                enabled = canSubmit,
                            )
                        else -> AppButton(key, { onDigit(key) }, keyModifier, style = AppButtonStyle.Outlined, enabled = enabled)
                    }
                }
            }
        }
    }
}
