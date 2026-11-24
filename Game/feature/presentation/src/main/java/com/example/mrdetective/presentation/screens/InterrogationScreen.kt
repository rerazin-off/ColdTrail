package com.example.mrdetective.presentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mrdetective.domain.models.Clue
import com.example.mrdetective.domain.models.DialogueMessage
import com.example.mrdetective.domain.models.MessageAuthor
import com.example.mrdetective.presentation.R
import com.example.mrdetective.presentation.ui.components.AppButton
import com.example.mrdetective.presentation.ui.components.AppButtonStyle
import com.example.mrdetective.presentation.ui.components.AppCard
import com.example.mrdetective.presentation.ui.components.ErrorNotice
import com.example.mrdetective.presentation.ui.components.AppTextField
import com.example.mrdetective.presentation.ui.components.TypewriterText
import com.example.mrdetective.presentation.ui.theme.AppShapes
import com.example.mrdetective.presentation.ui.theme.AppTheme
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
internal fun InterrogationScreen(viewModel: InterrogationViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val draft by viewModel.draft.collectAsStateWithLifecycle()
    val attachment by viewModel.attachment.collectAsStateWithLifecycle()
    val clues by viewModel.clues.collectAsStateWithLifecycle()
    var pickingClue by rememberSaveable { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val focus = LocalFocusManager.current
    val colors = AppTheme.colors

    LaunchedEffect(state.messages.lastOrNull()?.id) {
        if (state.messages.isNotEmpty()) listState.scrollToItem(state.messages.lastIndex)
    }
    LaunchedEffect(pickingClue) {
        if (pickingClue) viewModel.loadClues()
    }

    Column(Modifier.fillMaxSize().imePadding().padding(horizontal = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(painterResource(R.drawable.ic_chat_back), "Назад к делу", tint = colors.text)
            }
            Text("Допрос подозреваемого", style = MaterialTheme.typography.headlineMedium, color = colors.text)
        }

        val suspect = state.suspect
        if (state.loading) {
            ScreenProgress()
        } else if (suspect == null) {
            ScreenRetry("Не удалось открыть диалог.", viewModel::load)
        } else {
            AppCard(outlined = true, padding = 8.dp) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(46.dp),
                        shape = AppShapes.control,
                        color = colors.surface2,
                        border = BorderStroke(1.dp, colors.outline),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(suspect.name.take(1), color = colors.text, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    Column {
                        Text(suspect.name, style = MaterialTheme.typography.headlineMedium)
                        Text(
                            suspect.profile,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.text2,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            if (state.historyUnavailable && state.feedback == null) {
                ScreenRetry(state.historyError ?: "Не удалось обновить историю.", viewModel::load)
            }
            Surface(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(vertical = 8.dp),
                color = colors.surface,
                contentColor = colors.text,
                border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.45f)),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    contentPadding = PaddingValues(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (state.messages.isEmpty() && !state.historyUnavailable) {
                        item { Text("Задайте подозреваемому первый вопрос.", color = colors.text2) }
                    }
                    items(state.messages, key = { it.id }) { message ->
                        val animate = message.id in state.animatedMessageIds
                        if (message.author == MessageAuthor.PLAYER && animate) {
                            var visible by rememberSaveable { mutableStateOf(false) }
                            LaunchedEffect(Unit) { visible = true }
                            AnimatedVisibility(
                                visible = visible,
                                enter = fadeIn(tween(260)) + slideInVertically(tween(260)) { it / 4 },
                            ) {
                                DialogueBubble(message, suspect.name, animateText = false)
                            }
                        } else {
                            DialogueBubble(message, suspect.name, animateText = animate)
                        }
                    }
                }
            }

            Column(
                Modifier.padding(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                ErrorNotice(state.feedback, if (state.pendingFeedback) viewModel::retryPendingMessage else viewModel::send)
                if (attachment == null) {
                    Text("Скрепка — прикрепить улику к сообщению", style = MaterialTheme.typography.bodySmall, color = colors.text2)
                }
                attachment?.let { clue ->
                    AppCard(outlined = true, padding = 6.dp) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(painterResource(R.drawable.ic_chat_attach), null, Modifier.padding(horizontal = 6.dp), tint = colors.outline)
                            Text(
                                clue.title,
                                modifier = Modifier.weight(1f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodySmall,
                            )
                            IconButton(onClick = { viewModel.attach(null) }, enabled = !state.sending) {
                                Icon(painterResource(R.drawable.ic_chat_close), "Убрать улику", tint = colors.text)
                            }
                        }
                    }
                }
                AppTextField(
                    value = draft,
                    onValueChange = viewModel::updateDraft,
                    placeholder = "Ваш вопрос подозреваемому…",
                    enabled = !state.sending,
                    textStyle = MaterialTheme.typography.bodySmall,
                    trailingContent = {
                        IconButton(
                            enabled = !state.sending,
                            onClick = {
                                focus.clearFocus()
                                pickingClue = true
                            },
                        ) {
                            Icon(painterResource(R.drawable.ic_chat_attach), "Прикрепить улику", tint = colors.outline)
                        }
                        IconButton(
                            onClick = viewModel::send,
                            enabled = !state.sending && (draft.isNotBlank() || attachment != null),
                        ) {
                            if (state.sending) {
                                CircularProgressIndicator(Modifier.size(18.dp), color = colors.outline, strokeWidth = 2.dp)
                            } else {
                                Icon(
                                    painterResource(R.drawable.ic_chat_send),
                                    "Отправить сообщение",
                                    tint = if (draft.isNotBlank() || attachment != null) colors.text else colors.text2.copy(alpha = 0.45f),
                                )
                            }
                        }
                    },
                )
            }
        }
    }

    if (pickingClue) {
        CluePickerSheet(
            state = clues,
            onDismiss = { pickingClue = false },
            onRetry = viewModel::loadClues,
            onSelect = {
                viewModel.attach(it)
                pickingClue = false
            },
        )
    }
}

@Composable
private fun DialogueBubble(message: DialogueMessage, suspectName: String, animateText: Boolean) {
    val colors = AppTheme.colors
    val player = message.author == MessageAuthor.PLAYER
    val background = if (player) colors.surface2 else if (colors.isDark) colors.primary else colors.bg
    val foreground = if (!player && colors.isDark) colors.bg else colors.text
    Box(Modifier.fillMaxWidth(), contentAlignment = if (player) Alignment.CenterEnd else Alignment.CenterStart) {
        Surface(
            modifier = Modifier.fillMaxWidth(if (player) 0.72f else 0.82f),
            shape = AppShapes.control,
            color = background,
            contentColor = foreground,
            border = if (player) BorderStroke(1.dp, colors.outline) else null,
        ) {
            Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(if (player) "Вы" else suspectName, style = MaterialTheme.typography.bodySmall)
                TypewriterText(message.text, style = MaterialTheme.typography.bodyMedium, animate = animateText)
                messageTime(message.createdAt)?.let { time ->
                    Text(time, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

private val dialogueTimeFormat = DateTimeFormatter.ofPattern("HH:mm")

private fun messageTime(createdAt: String): String? = runCatching {
    val instant = createdAt.toLongOrNull()?.let(Instant::ofEpochMilli)
        ?: OffsetDateTime.parse(createdAt).toInstant()
    dialogueTimeFormat.format(instant.atZone(ZoneId.systemDefault()))
}.getOrNull()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CluePickerSheet(
    state: CluePickerState,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    onSelect: (Clue) -> Unit,
) {
    val colors = AppTheme.colors
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.bg,
        contentColor = colors.text,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Прикрепить улику", style = MaterialTheme.typography.headlineMedium)
                    Text("Выберите предмет из материалов дела.", style = MaterialTheme.typography.bodySmall, color = colors.text2)
                }
                IconButton(onClick = onDismiss) {
                    Icon(painterResource(R.drawable.ic_chat_close), "Закрыть выбор улики", tint = colors.text)
                }
            }
            when (state) {
                CluePickerState.Loading -> CircularProgressIndicator(color = colors.outline)
                CluePickerState.Error -> ScreenRetry("Не удалось загрузить улики.", onRetry)
                is CluePickerState.Content -> if (state.clues.isEmpty()) {
                    Text("В этом расследовании пока нет собранных улик.", color = colors.text2)
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.heightIn(max = 330.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        gridItems(state.clues, key = { it.id }) { clue ->
                            AppCard(outlined = true, padding = 8.dp) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Box(Modifier.size(32.dp).background(colors.surface2, AppShapes.control))
                                        Column {
                                            Text(clue.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                            Text(clue.description, style = MaterialTheme.typography.bodySmall, color = colors.text2, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                    AppButton("Выбрать", { onSelect(clue) }, style = AppButtonStyle.Outlined, compact = true)
                                }
                            }
                        }
                    }
                    Text("Выбранная улика появится в сообщении.", style = MaterialTheme.typography.bodySmall, color = colors.text2)
                }
            }
        }
    }
}
