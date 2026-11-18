package com.example.mrdetective.presentation.ui.components
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mrdetective.presentation.ui.theme.AppShapes
import com.example.mrdetective.presentation.ui.theme.AppTheme
import com.example.mrdetective.presentation.ui.theme.Spacing

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val c = AppTheme.colors
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.card,
        color = c.surface,
        contentColor = c.text,
    ) {
        Box(modifier = Modifier.padding(Spacing.md)) { content() }
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
            AppButton(text = "Начать расследование", onClick = onStart)
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
    avatar: @Composable () -> Unit = { AvatarPlaceholder(name) },
) {
    AppCard(modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                avatar()
                Column {
                    Text(name, style = MaterialTheme.typography.titleMedium)
                    Text(description, style = MaterialTheme.typography.bodyMedium, color = AppTheme.colors.text2)
                }
            }
            AppButton(text = "Допросить", onClick = onInterrogate, style = AppButtonStyle.Accent)
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
        modifier = Modifier.size(56.dp).background(c.surface2, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(name.take(1), style = MaterialTheme.typography.titleMedium, color = c.text)
    }
}

@Composable
private fun ImagePlaceholder() {
    Box(modifier = Modifier.size(64.dp).background(AppTheme.colors.surface2, AppShapes.control))
}
