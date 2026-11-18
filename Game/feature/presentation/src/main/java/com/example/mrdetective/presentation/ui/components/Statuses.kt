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
import androidx.compose.ui.unit.dp
import com.example.mrdetective.presentation.ui.theme.AppTheme
import com.example.mrdetective.presentation.ui.theme.Spacing

enum class ChipStatus(val label: String) {
    ClueFound("Улика найдена"),
    Available("Доступно"),
    Done("Выполнено"),
    Error("Ошибка"),
}


@Composable
fun Statuses(
    status: ChipStatus,
    modifier: Modifier = Modifier,
    text: String = status.label,
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
        color = color,
        modifier =
            modifier
                .background(color.copy(alpha = 0.16f), shape)
                .border(1.dp, color, shape)
                .padding(horizontal = Spacing.sm + Spacing.xs, vertical = Spacing.xs),
    )
}
