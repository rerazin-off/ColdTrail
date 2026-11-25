package com.example.mrdetective.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.mrdetective.presentation.ui.theme.AppShapes
import com.example.mrdetective.presentation.ui.theme.AppTheme
import com.example.mrdetective.presentation.ui.theme.Sizes
import com.example.mrdetective.presentation.ui.theme.Spacing

/**
 * Отображает поле ввода с подписью, доступной службам специальных возможностей.
 *
 * @param value Текущий текст, которым управляет вызывающий экран.
 * @param onValueChange Получает новый текст после редактирования.
 * @param placeholder Подпись пустого поля и его имя для TalkBack.
 * @param modifier Модификатор внешнего контейнера.
 * @param singleLine Ограничивает поле одной строкой.
 * @param maxLines Максимальное количество видимых строк многострочного поля.
 * @param enabled Разрешает ввод и фокусировку.
 * @param keyboardOptions Настройки экранной клавиатуры.
 * @param visualTransformation Отображение введённого текста, например маскирование пароля.
 * @param textStyle Стиль текста из темы приложения.
 * @param minLines Минимальное количество видимых строк многострочного поля.
 * @param trailingContent Дополнительные элементы справа от текста.
 */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else 3,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    minLines: Int = 1,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    val c = AppTheme.colors
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        enabled = enabled,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        textStyle = textStyle.copy(color = c.text),
        cursorBrush = SolidColor(c.accent),
        modifier =
            modifier.fillMaxWidth().semantics {
                if (placeholder.isNotBlank()) contentDescription = placeholder
            },
        decorationBox = { innerTextField ->
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = Sizes.controlHeight)
                        .background(c.surface, AppShapes.control)
                        .border(1.dp, c.outline, AppShapes.control)
                        .padding(start = Spacing.md, end = if (trailingContent == null) Spacing.md else Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        Text(text = placeholder, style = textStyle, color = c.text2)
                    }
                    innerTextField()
                }
                trailingContent?.invoke()
            }
        },
    )
}
