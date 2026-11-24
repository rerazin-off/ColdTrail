package com.example.mrdetective.presentation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.mrdetective.domain.models.CameraChallenge
import com.example.mrdetective.domain.repositories.SceneClueImage
import com.example.mrdetective.presentation.R
import com.example.mrdetective.presentation.ui.components.AppCard
import com.example.mrdetective.presentation.ui.components.ErrorNotice
import com.example.mrdetective.presentation.ui.theme.AppShapes
import com.example.mrdetective.presentation.ui.theme.AppTheme

private val hidingPlaces = listOf(0.24f to 0.72f, 0.62f to 0.72f, 0.88f to 0.56f)
private val scenePlaceholders = listOf(
    R.drawable.ic_camera_clue_placeholder,
    R.drawable.ic_scene_clue_note,
    R.drawable.ic_scene_clue_photo,
)

@Composable
internal fun SceneClueSearch(
    challenge: CameraChallenge,
    state: CameraClueUiState,
    onLoad: () -> Unit,
    onSelect: (Int) -> Unit,
    onCameraSettings: (() -> Unit)?,
) {
    LaunchedEffect(challenge.id) { onLoad() }
    val colors = AppTheme.colors
    val items = state.sceneItems
    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AppCard(outlined = true) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                val targetIndex = items?.indexOfFirst { it.clueId == challenge.clueId } ?: -1
                if (items != null && targetIndex >= 0) {
                    SceneArtwork(items[targetIndex], targetIndex, "Образец улики", Modifier.size(42.dp))
                }
                Column {
                    Text("Найдите на складе", style = MaterialTheme.typography.bodySmall, color = colors.text2)
                    Text(challenge.clueTitle, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
        if (items == null) {
            CircularProgressIndicator(color = colors.accent)
        } else {
            BoxWithConstraints(
                Modifier.fillMaxWidth().aspectRatio(1920f / 1136f).clip(AppShapes.card),
            ) {
                Image(
                    painter = painterResource(R.drawable.clue_search_warehouse),
                    contentDescription = "Склад",
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.FillBounds,
                )
                val touchSize = 48.dp
                val artworkSize = (maxWidth * 0.042f).coerceIn(12.dp, 27.dp)
                items.forEachIndexed { index, item ->
                    val (x, y) = hidingPlaces[index]
                    Box(
                        Modifier.absoluteOffset(maxWidth * x - touchSize / 2, maxHeight * y - touchSize / 2)
                            .size(touchSize)
                            .clickable(enabled = !state.capturing, role = Role.Button) { onSelect(index) },
                        contentAlignment = Alignment.Center,
                    ) {
                        SceneArtwork(item, index, "Предмет ${index + 1}", Modifier.size(artworkSize))
                    }
                }
            }
        }
        val targetIndex = items?.indexOfFirst { it.clueId == challenge.clueId } ?: -1
        ErrorNotice(state.sceneMessage, if (state.sceneRetry && targetIndex >= 0) ({ onSelect(targetIndex) }) else null)
        Text(
            if (state.capturing) "Проверяем улику…" else "Нажмите на предмет, который совпадает с образцом.",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.text2,
        )
        onCameraSettings?.let { TextButton(onClick = it, enabled = !state.capturing) { Text("Разрешение камеры") } }
    }
}

@Composable
private fun SceneArtwork(item: SceneClueImage, index: Int, label: String, modifier: Modifier) {
    val placeholder = painterResource(scenePlaceholders[index])
    AsyncImage(
        model = item.imageUrl,
        contentDescription = label,
        modifier = modifier,
        placeholder = placeholder,
        fallback = placeholder,
        error = placeholder,
        contentScale = ContentScale.Fit,
    )
}
