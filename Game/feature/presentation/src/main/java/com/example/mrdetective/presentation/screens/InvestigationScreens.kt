package com.example.mrdetective.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.example.mrdetective.presentation.ui.components.AppButton
import com.example.mrdetective.presentation.ui.components.AppButtonStyle
import com.example.mrdetective.presentation.ui.components.AppCard
import com.example.mrdetective.presentation.ui.components.SuspectCard
import com.example.mrdetective.presentation.ui.theme.AppTheme

@Composable
internal fun InvestigationScreen(
    viewModel: InvestigationViewModel,
    onBack: () -> Unit,
    onStatusChanged: (Int, String) -> Unit,
    onMorse: (Int) -> Unit,
    onAccusation: (Int) -> Unit,
    onInterrogate: (Int, Int) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refreshProgress() }
    LaunchedEffect(state.case?.id, state.status) {
        val caseId = state.case?.id ?: return@LaunchedEffect
        val status = state.status ?: return@LaunchedEffect
        onStatusChanged(caseId, status)
    }
    val colors = AppTheme.colors
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        TextButton(onClick = onBack, modifier = Modifier.heightIn(min = 48.dp)) {
            Text(
                state.case?.let { "‹   Дело №%02d".format(it.id) } ?: "‹   К списку дел",
                style = MaterialTheme.typography.headlineMedium,
                color = colors.text,
            )
        }
        if (state.loading) {
            ScreenProgress()
        } else {
            val case = state.case
            if (case == null) {
                ScreenRetry(state.error ?: "Дело недоступно.", viewModel::load)
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        Text(case.title, style = MaterialTheme.typography.headlineLarge)
                    }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(case.introduction, style = MaterialTheme.typography.bodyMedium, color = colors.text2)
                            if (case.objective.isNotBlank()) Text(case.objective)
                        }
                    }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Этапы расследования", style = MaterialTheme.typography.bodySmall, color = colors.text2)
                            InvestigationStep(1, "Начать расследование", if (state.started) "Выполнено" else "Доступно")
                            InvestigationStep(
                                2,
                                "Собрать улики",
                                when {
                                    state.collectedClueCount > 0 -> "Найдено улик: ${state.collectedClueCount}"
                                    state.started -> "Доступно"
                                    else -> "После начала расследования"
                                },
                            )
                        }
                    }
                    item { Text("Персонажи", style = MaterialTheme.typography.bodySmall, color = colors.text2) }
                    if (state.suspectsUnavailable) {
                        item { ScreenRetry("Не удалось загрузить подозреваемых.", viewModel::load) }
                    } else if (state.suspects.isEmpty()) {
                        item { Text("Подозреваемых пока нет.", color = AppTheme.colors.text2) }
                    } else {
                        items(state.suspects, key = { it.id }) { suspect ->
                            SuspectCard(
                                name = suspect.name,
                                description = suspect.profile,
                                onInterrogate = { onInterrogate(case.id, suspect.id) },
                                enabled = state.started,
                            )
                        }
                    }
                    item {
                        state.error?.let { Text(it, color = AppTheme.colors.error) }
                        if (!state.started) {
                            AppButton(
                                text = if (state.starting) "Открываем дело…" else "Начать расследование",
                                onClick = viewModel::start,
                                enabled = !state.starting,
                                style = AppButtonStyle.Accent,
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (state.status !in setOf("solved", "failed", "closed")) {
                                    AppButton("Добыть улику", { onMorse(case.id) }, style = AppButtonStyle.Accent)
                                }
                                AppButton(
                                    if (state.status in setOf("solved", "failed", "closed")) "Итог расследования" else "Выдвинуть обвинение",
                                    { onAccusation(case.id) },
                                    style = AppButtonStyle.Outlined,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InvestigationStep(number: Int, title: String, progress: String) {
    val colors = AppTheme.colors
    AppCard(outlined = true, padding = 8.dp) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(number.toString(), style = MaterialTheme.typography.labelLarge, color = colors.text2)
            Column {
                Text(title, style = MaterialTheme.typography.bodySmall)
                Text(progress, style = MaterialTheme.typography.bodySmall, color = colors.text2)
            }
        }
    }
}

@Composable
internal fun ScreenProgress() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = AppTheme.colors.accent)
    }
}

@Composable
internal fun ScreenRetry(message: String, onRetry: () -> Unit) {
    Column(Modifier.padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(message, color = AppTheme.colors.error)
        AppButton("Повторить", onRetry, style = AppButtonStyle.Outlined)
    }
}
