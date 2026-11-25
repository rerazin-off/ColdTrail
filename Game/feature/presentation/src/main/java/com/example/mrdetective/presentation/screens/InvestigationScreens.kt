package com.example.mrdetective.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mrdetective.domain.models.Case
import com.example.mrdetective.presentation.R
import com.example.mrdetective.presentation.ui.components.AppButton
import com.example.mrdetective.presentation.ui.components.AppButtonStyle
import com.example.mrdetective.presentation.ui.components.AppCard
import com.example.mrdetective.presentation.ui.components.AppTextField
import com.example.mrdetective.presentation.ui.components.ErrorNotice
import com.example.mrdetective.presentation.ui.components.SuspectCard
import com.example.mrdetective.presentation.ui.theme.AppShapes
import com.example.mrdetective.presentation.ui.theme.AppTheme
import com.example.mrdetective.presentation.ui.theme.usesWideLayout

@Composable
internal fun InvestigationScreen(
    viewModel: InvestigationViewModel,
    onBack: () -> Unit,
    onStatusChanged: (Int, String) -> Unit,
    onFindClue: (Int) -> Unit,
    onAccusation: (Int) -> Unit,
    onInterrogate: (Int, Int) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val note by viewModel.note.collectAsStateWithLifecycle()
    val noteEditorOpen by viewModel.noteEditorOpen.collectAsStateWithLifecycle()
    if (noteEditorOpen) InvestigationNoteEditor(viewModel)
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
                state.case?.let { stringResource(R.string.case_number, it.id) } ?: stringResource(R.string.back_to_cases),
                style = MaterialTheme.typography.headlineMedium,
                color = colors.text,
            )
        }
        if (state.loading) {
            ScreenProgress()
        } else {
            val case = state.case
            if (case == null) {
                ScreenRetry(state.error ?: stringResource(R.string.case_unavailable), viewModel::load)
            } else {
                if (state.suspectsUnavailable && state.error == null) {
                    ScreenRetry(state.suspectsError ?: stringResource(R.string.suspects_load_error), viewModel::load)
                }
                val details: LazyListScope.() -> Unit = {
                    investigationDetails(case, state)
                    if (state.started) {
                        item {
                            AppButton(
                                stringResource(R.string.add_note),
                                viewModel::openNoteEditor,
                                style = AppButtonStyle.Outlined,
                            )
                        }
                    }
                    note?.let { saved ->
                        item {
                            AppCard(Modifier.fillMaxWidth(), outlined = true) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(stringResource(R.string.latest_note), style = MaterialTheme.typography.titleMedium)
                                    Text(saved.text)
                                }
                            }
                        }
                    }
                }
                val actions: LazyListScope.() -> Unit = {
                    investigationActions(case.id, state, viewModel::start, onFindClue, onAccusation)
                }
                BoxWithConstraints(Modifier.weight(1f)) {
                    if (usesWideLayout() && maxWidth >= 620.dp) {
                        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            InvestigationPane(Modifier.weight(1.05f)) {
                                details()
                                actions()
                            }
                            InvestigationPane(Modifier.weight(1f)) { investigationSuspects(case.id, state, onInterrogate) }
                        }
                    } else {
                        InvestigationPane(Modifier.fillMaxSize()) {
                            details()
                            investigationSuspects(case.id, state, onInterrogate)
                            actions()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InvestigationNoteEditor(viewModel: InvestigationViewModel) {
    val draft by viewModel.noteDraft.collectAsStateWithLifecycle()
    val saving by viewModel.noteSaving.collectAsStateWithLifecycle()
    val error by viewModel.noteError.collectAsStateWithLifecycle()
    val colors = AppTheme.colors
    AlertDialog(
        onDismissRequest = viewModel::closeNoteEditor,
        title = {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.add_note), modifier = Modifier.weight(1f))
                IconButton(onClick = viewModel::closeNoteEditor, enabled = !saving) {
                    Icon(
                        painterResource(R.drawable.ic_chat_close),
                        contentDescription = stringResource(R.string.close),
                        tint = colors.text,
                    )
                }
            }
        },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppTextField(
                    value = draft,
                    onValueChange = viewModel::updateNoteDraft,
                    placeholder = stringResource(R.string.note_input),
                    modifier = Modifier.heightIn(min = 224.dp),
                    singleLine = false,
                    maxLines = 12,
                    minLines = 9,
                    enabled = !saving,
                )
                error?.let { Text(it, color = colors.error, style = MaterialTheme.typography.bodySmall) }
            }
        },
        confirmButton = {
            AppButton(
                stringResource(if (error == null) R.string.save_note else R.string.retry),
                viewModel::submitNote,
                enabled = draft.isNotBlank() && !saving,
            )
        },
        shape = AppShapes.card,
        containerColor = colors.surface,
        titleContentColor = colors.text,
        textContentColor = colors.text,
    )
}

@Composable
private fun InvestigationPane(
    modifier: Modifier,
    content: LazyListScope.() -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content,
    )
}

private fun LazyListScope.investigationDetails(
    case: Case,
    state: InvestigationState,
) {
    item {
        Text(case.title, style = MaterialTheme.typography.headlineLarge)
    }
    item {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(case.introduction, style = MaterialTheme.typography.bodyMedium, color = AppTheme.colors.text2)
            if (case.objective.isNotBlank()) Text(case.objective)
        }
    }
    item {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(stringResource(R.string.investigation_steps), style = MaterialTheme.typography.bodySmall, color = AppTheme.colors.text2)
            InvestigationStep(
                1,
                stringResource(R.string.start_investigation),
                if (state.started) stringResource(R.string.done) else stringResource(R.string.available),
            )
            InvestigationStep(
                2,
                stringResource(R.string.collect_clues),
                when {
                    state.totalClueCount != null ->
                        stringResource(
                            R.string.clues_progress,
                            state.collectedClueCount,
                            state.totalClueCount ?: 0,
                        )
                    state.collectedClueCount > 0 -> stringResource(R.string.clues_count, state.collectedClueCount)
                    state.started -> stringResource(R.string.available)
                    else -> stringResource(R.string.after_investigation_start)
                },
            )
        }
    }
}

private fun LazyListScope.investigationActions(
    caseId: Int,
    state: InvestigationState,
    onStart: () -> Unit,
    onFindClue: (Int) -> Unit,
    onAccusation: (Int) -> Unit,
) {
    item {
        ErrorNotice(state.error, onStart)
        if (!state.started) {
            AppButton(
                text = if (state.starting) stringResource(R.string.opening_case) else stringResource(R.string.start_investigation),
                onClick = onStart,
                enabled = !state.starting,
                style = AppButtonStyle.Accent,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (state.hasRemainingClues && state.status !in setOf("solved", "failed", "closed")) {
                    AppButton(stringResource(R.string.find_clue), { onFindClue(caseId) }, style = AppButtonStyle.Accent)
                }
                AppButton(
                    if (state.status in setOf("solved", "failed", "closed")) {
                        stringResource(
                            R.string.investigation_result,
                        )
                    } else {
                        stringResource(R.string.make_accusation)
                    },
                    { onAccusation(caseId) },
                    style = AppButtonStyle.Outlined,
                )
            }
        }
    }
}

private fun LazyListScope.investigationSuspects(
    caseId: Int,
    state: InvestigationState,
    onInterrogate: (Int, Int) -> Unit,
) {
    item { Text(stringResource(R.string.characters), style = MaterialTheme.typography.bodySmall, color = AppTheme.colors.text2) }
    if (state.suspects.isEmpty() && !state.suspectsUnavailable) {
        item { Text(stringResource(R.string.no_suspects), color = AppTheme.colors.text2) }
    } else {
        items(state.suspects, key = { it.id }) { suspect ->
            SuspectCard(
                name = suspect.name,
                description = suspect.profile,
                onInterrogate = { onInterrogate(caseId, suspect.id) },
                enabled = state.started,
            )
        }
    }
}

@Composable
private fun InvestigationStep(
    number: Int,
    title: String,
    progress: String,
) {
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
internal fun ScreenRetry(
    message: String,
    onRetry: () -> Unit,
) {
    ErrorNotice(message, onRetry)
}
