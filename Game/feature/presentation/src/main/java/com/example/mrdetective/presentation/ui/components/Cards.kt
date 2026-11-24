package com.example.mrdetective.presentation.ui.components
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.mrdetective.presentation.R
import com.example.mrdetective.presentation.ui.theme.AppShapes
import com.example.mrdetective.presentation.ui.theme.AppTheme
import com.example.mrdetective.presentation.ui.theme.Spacing

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    outlined: Boolean = false,
    padding: androidx.compose.ui.unit.Dp = Spacing.md,
    content: @Composable () -> Unit,
) {
    val c = AppTheme.colors
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.card,
        color = c.surface,
        contentColor = c.text,
        border = if (outlined) BorderStroke(1.dp, c.outline) else null,
    ) {
        Box(modifier = Modifier.padding(padding)) { content() }
    }
}

@Composable
fun CaseCard(
    title: String,
    intro: String,
    onStart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Text(title, style = MaterialTheme.typography.headlineMedium)
            Text(intro, style = MaterialTheme.typography.bodyLarge, color = AppTheme.colors.text2)
            AppButton(text = stringResource(R.string.start_investigation), onClick = onStart)
        }
    }
}

//Карточка подозреваемого: аватар (слот), имя, описание, кнопка перехода к допросу.
@Composable
fun SuspectCard(
    name: String,
    description: String,
    onInterrogate: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    avatar: @Composable () -> Unit = { AvatarPlaceholder(name) },
) {
    AppCard(modifier, outlined = true, padding = Spacing.sm) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.weight(0.34f), contentAlignment = Alignment.Center) {
                avatar()
            }
            Column(Modifier.weight(0.66f), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Text(name, style = MaterialTheme.typography.headlineMedium)
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.colors.text2,
                )
                AppButton(stringResource(R.string.open_dialogue), onInterrogate, enabled = enabled, compact = true)
            }
        }
    }
}

//Карточка улики: изображение (слот), название, описание.

@Composable
fun ClueCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    image: @Composable () -> Unit = { ImagePlaceholder() },
) {
    AppCard(modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            image()
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(description, style = MaterialTheme.typography.bodyMedium, color = AppTheme.colors.text2)
            }
        }
    }
}

@Composable
private fun AvatarPlaceholder(name: String) {
    val c = AppTheme.colors
    Box(
        modifier = Modifier.fillMaxWidth().height(118.dp).background(c.primary, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(name.take(1), style = MaterialTheme.typography.titleMedium, color = c.onPrimary)
    }
}

@Composable
private fun ImagePlaceholder() {
    Box(modifier = Modifier.size(64.dp).background(AppTheme.colors.surface2, AppShapes.control))
}
