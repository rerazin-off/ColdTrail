package com.example.mrdetective.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mrdetective.domain.models.MessageAuthor
import com.example.mrdetective.presentation.ui.components.AppButton
import com.example.mrdetective.presentation.ui.components.AppButtonStyle
import com.example.mrdetective.presentation.ui.components.AppCard
import com.example.mrdetective.presentation.ui.components.AppTextField
import com.example.mrdetective.presentation.ui.components.SuspectCard
import com.example.mrdetective.presentation.ui.theme.AppShapes
import com.example.mrdetective.presentation.ui.theme.AppTheme

@Composable
internal fun InvestigationScreen(
    viewModel: InvestigationViewModel,
    onBack: () -> Unit,
    onStatusChanged: (Int, String) -> Unit,
    onInterrogate: (Int, Int) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
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
                        items(state.suspects.chunked(2)) { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                row.forEach { suspect ->
                                    SuspectCard(
                                        name = suspect.name,
                                        description = suspect.profile,
                                        onInterrogate = { onInterrogate(case.id, suspect.id) },
                                        modifier = Modifier.weight(1f),
                                        enabled = state.started,
                                    )
                                }
                                if (row.size == 1) Box(Modifier.weight(1f))
                            }
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
internal fun InterrogationScreen(viewModel: InterrogationViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val draft by viewModel.draft.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val colors = AppTheme.colors
    LaunchedEffect(state.messages.lastOrNull()?.id) {
        if (state.messages.isNotEmpty()) listState.scrollToItem(state.messages.lastIndex)
    }
    Column(Modifier.fillMaxSize().imePadding().padding(horizontal = 20.dp)) {
        TextButton(onClick = onBack) { Text("‹  К делу") }
        Text(state.suspect?.name ?: "Допрос", style = MaterialTheme.typography.displayLarge)
        state.suspect?.profile?.takeIf { it.isNotBlank() }?.let {
            Text(it, color = colors.text2, maxLines = 3, overflow = TextOverflow.Ellipsis)
        }
        when {
            state.loading -> ScreenProgress()
            state.suspect == null -> ScreenRetry("Не удалось открыть диалог.", viewModel::load)
            else -> {
                if (state.historyUnavailable) {
                    ScreenRetry("Не удалось обновить историю.", viewModel::load)
                }
                if (state.messages.isEmpty() && !state.historyUnavailable) {
                    Text("Задайте подозреваемому первый вопрос.", Modifier.padding(top = 16.dp), color = colors.text2)
                }
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    state = listState,
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(state.messages, key = { it.id }) { message ->
                        val player = message.author == MessageAuthor.PLAYER
                        Box(Modifier.fillMaxWidth(), contentAlignment = if (player) Alignment.CenterEnd else Alignment.CenterStart) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(0.85f).widthIn(max = 420.dp),
                                shape = AppShapes.card,
                                color = if (player) colors.surface2 else colors.surface,
                                contentColor = colors.text,
                            ) {
                                Text(message.text, Modifier.padding(12.dp))
                            }
                        }
                    }
                }
                state.feedback?.let { Text(it, color = colors.text2) }
                Column(
                    Modifier.padding(bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AppTextField(
                        value = draft,
                        onValueChange = viewModel::updateDraft,
                        placeholder = "Ваш вопрос",
                        enabled = !state.sending,
                    )
                    AppButton(
                        text = if (state.sending) "Отправляем…" else "Отправить",
                        onClick = viewModel::send,
                        enabled = !state.sending && draft.isNotBlank(),
                    )
                }
            }
        }
    }
}

@Composable
private fun ScreenProgress() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = AppTheme.colors.accent)
    }
}

@Composable
private fun ScreenRetry(message: String, onRetry: () -> Unit) {
    Column(Modifier.padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(message, color = AppTheme.colors.error)
        AppButton("Повторить", onRetry, style = AppButtonStyle.Outlined)
    }
}
