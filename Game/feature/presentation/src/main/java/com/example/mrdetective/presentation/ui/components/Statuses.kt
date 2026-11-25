package com.example.mrdetective.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.mrdetective.presentation.R
import com.example.mrdetective.presentation.ui.theme.AppTheme
import com.example.mrdetective.presentation.ui.theme.Spacing

/**
 * Связывает статус расследования с локализованной подписью.
 *
 * @property label Ресурс локализованной подписи.
 */
enum class ChipStatus(val label: Int) {
    ClueFound(R.string.clue_found),
    Available(R.string.available),
    Done(R.string.done),
    Error(R.string.error),
}

/** Отображает статус в оформленной плашке. */
@Composable
fun Statuses(
    status: ChipStatus,
    modifier: Modifier = Modifier,
    text: String = stringResource(status.label),
) {
    val c = AppTheme.colors
    val color: Color =
        when (status) {
            ChipStatus.ClueFound -> c.accent
            ChipStatus.Available -> c.text2
            ChipStatus.Done -> c.success
            ChipStatus.Error -> c.error
        }
    val shape = RoundedCornerShape(50)
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = if (status == ChipStatus.ClueFound) c.text else color,
        modifier =
            modifier
                .background(color.copy(alpha = 0.16f), shape)
                .border(1.dp, color, shape)
                .padding(horizontal = Spacing.sm + Spacing.xs, vertical = Spacing.xs),
    )
}
