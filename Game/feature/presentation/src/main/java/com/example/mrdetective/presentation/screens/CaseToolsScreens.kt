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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.example.mrdetective.domain.models.Suspect
import com.example.mrdetective.presentation.R
import com.example.mrdetective.presentation.ui.components.AppButton
import com.example.mrdetective.presentation.ui.components.AppButtonStyle
import com.example.mrdetective.presentation.ui.components.ClueRewardDialog
import com.example.mrdetective.presentation.ui.components.AppCard
import com.example.mrdetective.presentation.ui.components.AppTextField
import com.example.mrdetective.presentation.ui.components.MorseAlphabetPanel
import com.example.mrdetective.presentation.ui.components.ClueCard
import com.example.mrdetective.presentation.ui.components.TypewriterText
import com.example.mrdetective.presentation.ui.theme.AppShapes
import com.example.mrdetective.presentation.ui.theme.AppTheme
import kotlinx.coroutines.delay

@Composable
internal fun CaseToolHeader(title: String, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onBack) {
            Icon(painterResource(R.drawable.ic_chat_back), "Назад", tint = AppTheme.colors.text)
        }
        Text(title, style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
internal fun ProfileScreen(
    viewModel: ProfileStatisticsViewModel,
    auth: AuthUiState,
    onBack: () -> Unit,
    onSettings: () -> Unit,
    onSignOut: () -> Unit,
) {
    val statisticsState by viewModel.state.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.load() }
    val colors = AppTheme.colors
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.fillMaxWidth()) {
            CaseToolHeader("Профиль", onBack)
            IconButton(onSettings, Modifier.align(Alignment.CenterEnd)) {
                Icon(painterResource(R.drawable.ic_settings), "Настройки", tint = colors.text)
            }
        }
        Spacer(Modifier.height(42.dp))
        Surface(
            modifier = Modifier.size(120.dp),
            shape = CircleShape,
            color = colors.surface2,
            border = BorderStroke(1.dp, colors.outline),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("СД", style = MaterialTheme.typography.displayLarge, color = colors.accent)
            }
        }
        Spacer(Modifier.height(20.dp))
        Text("Следователь", style = MaterialTheme.typography.headlineLarge)
        Text("Ваш прогресс в расследованиях", style = MaterialTheme.typography.bodySmall, color = colors.text2)
        Spacer(Modifier.height(32.dp))
        when (val current = statisticsState) {
            ProfileStatisticsState.Loading -> CircularProgressIndicator(color = colors.accent)
            ProfileStatisticsState.Error -> ScreenRetry("Не удалось загрузить статистику.", viewModel::load)
            is ProfileStatisticsState.Content -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val stats = current.statistics
                listOf(stats.total to "Всего дел", stats.active to "Активных", stats.solved to "Раскрыто").forEach { (value, label) ->
                    AppCard(Modifier.weight(1f), outlined = true, padding = 8.dp) {
                        Column(
                            Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(value.toString(), color = colors.accent, style = MaterialTheme.typography.headlineMedium)
                            Text(label, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        auth.message?.let { Text(it, color = colors.error) }
        AppButton(
            text = if (auth.busy) "Выходим…" else "Выйти из аккаунта",
            onClick = onSignOut,
            enabled = !auth.busy,
            style = AppButtonStyle.Outlined,
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
internal fun InventoryScreen(viewModel: InventoryViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = AppTheme.colors
    var selectedClueId by rememberSaveable { mutableStateOf<Int?>(null) }
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        CaseToolHeader("Инвентарь улик", onBack)
        Text("Материалы дела", style = MaterialTheme.typography.displayLarge)
        Text("Найденные предметы и доказательства.", color = colors.text2, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(20.dp))
        when (val current = state) {
            CluePickerState.Loading -> ScreenProgress()
            CluePickerState.Error -> ScreenRetry("Не удалось загрузить инвентарь.", viewModel::load)
            is CluePickerState.Content -> if (current.clues.isEmpty()) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    AppCard(outlined = true) {
                        Column(
                            Modifier.fillMaxWidth().padding(vertical = 36.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Icon(painterResource(R.drawable.ic_inventory), null, tint = colors.accent)
                            Text("Пока нет улик", style = MaterialTheme.typography.headlineLarge)
                            Text(
                                "Осмотрите место происшествия, чтобы найти предметы.",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = colors.text2,
                            )
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(current.clues, key = { it.id }) { clue ->
                        Surface(
                            onClick = { selectedClueId = clue.id },
                            shape = AppShapes.card,
                            color = colors.surface,
                            border = BorderStroke(1.dp, colors.outline),
                        ) {
                            Column(
                                Modifier.heightIn(min = 140.dp).padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Box(Modifier.size(40.dp).background(colors.surface2, AppShapes.control))
                                Text(clue.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Text(clue.description, style = MaterialTheme.typography.bodySmall, color = colors.text2, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
                current.clues.firstOrNull { it.id == selectedClueId }?.let { clue ->
                    AlertDialog(
                        onDismissRequest = { selectedClueId = null },
                        title = { Text("Материал дела") },
                        text = { ClueCard(clue.title, clue.description) },
                        confirmButton = { AppButton("Закрыть", { selectedClueId = null }) },
                        containerColor = colors.bg,
                    )
                }
            }
        }
    }
}

@Composable
internal fun MorseScreen(
    viewModel: VibrationViewModel,
    game: MorseChallengeViewModel,
    onBack: () -> Unit,
) {
    val puzzle by game.state.collectAsStateWithLifecycle()
    val answer by game.answer.collectAsStateWithLifecycle()
    val challenge = puzzle.challenge
    val vibration by viewModel.state.collectAsStateWithLifecycle()
    var showVibrationOptions by rememberSaveable { mutableStateOf(false) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.load() }
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) { viewModel.stopPlayback() }
    LaunchedEffect(challenge?.id, vibration.loading, vibration.enabled, vibration.settingsError) {
        if (challenge != null && !vibration.loading && vibration.enabled == null && vibration.settingsError == null) {
            showVibrationOptions = true
        }
    }
    val colors = AppTheme.colors
    val solved = puzzle.solved
    if (solved) {
        ClueRewardDialog(puzzle.reward?.title, puzzle.reward?.description, onBack)
    }
    val border = when {
        solved -> colors.success
        puzzle.answerError != null -> colors.error
        else -> colors.outline
    }

    fun checkCode() {
        viewModel.stopPlayback()
        game.submit()
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.fillMaxWidth()) {
            CaseToolHeader("Головоломка", onBack)
            IconButton(
                onClick = {
                    viewModel.stopPlayback()
                    showVibrationOptions = true
                },
                modifier = Modifier.align(Alignment.CenterEnd),
            ) {
                Icon(painterResource(R.drawable.ic_help), "Настройки вибрации", tint = colors.text)
            }
        }
        when {
            puzzle.loading -> CircularProgressIndicator(color = colors.accent)
            puzzle.loadError != null -> ScreenRetry(puzzle.loadError.orEmpty(), game::load)
            challenge == null -> AppCard(outlined = true) {
                Text("Для этого дела больше нет доступных загадок Морзе.")
            }
            else -> {
                Text(challenge.title, style = MaterialTheme.typography.displayLarge)
                Text("Расшифруйте сигнал Морзе.", style = MaterialTheme.typography.bodySmall, color = colors.text2)
                Spacer(Modifier.height(12.dp))
                AppCard(outlined = true) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        challenge.hint?.takeIf { it.isNotBlank() }?.let {
                            Text(it, style = MaterialTheme.typography.bodySmall, color = colors.text2)
                        }
                        when {
                            vibration.loading -> Text("Загружаем настройку сигнала…", style = MaterialTheme.typography.bodySmall)
                            vibration.usesVibration -> AppButton(
                                if (vibration.playing) "Проигрываем сигнал…" else "Проиграть код",
                                { viewModel.play(challenge.signal) },
                                style = AppButtonStyle.Outlined,
                                enabled = !vibration.playing && !vibration.saving && !solved && !puzzle.submitting,
                            )
                            vibration.enabled != null || vibration.settingsError != null -> Text(
                                challenge.signal.replace(" ", " / "),
                                style = MaterialTheme.typography.headlineMedium,
                            )
                        }
                        vibration.playbackError?.let { Text(it, color = colors.text2, style = MaterialTheme.typography.bodySmall) }
                        if (challenge.numericOnly) {
                            (0 until challenge.answerLength).toList().chunked(6).forEach { slots ->
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    slots.forEach { index ->
                                        Surface(
                                            modifier = Modifier.weight(1f).height(58.dp),
                                            shape = AppShapes.control,
                                            color = if (solved) colors.success else colors.surface2,
                                            contentColor = if (solved) colors.bg else colors.text,
                                            border = BorderStroke(1.dp, border),
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(answer.getOrNull(index)?.toString() ?: "–", style = MaterialTheme.typography.headlineMedium)
                                            }
                                        }
                                    }
                                }
                            }
                        } else if (!solved) {
                            AppTextField(
                                value = answer,
                                onValueChange = game::updateAnswer,
                                placeholder = "Ответ латиницей или цифрами",
                                enabled = !puzzle.submitting,
                            )
                            Text("Знаков в ответе: ${challenge.answerLength}", style = MaterialTheme.typography.bodySmall, color = colors.text2)
                        }
                        if (solved) {
                            Text("Улика добавлена в инвентарь.", color = colors.success, style = MaterialTheme.typography.bodyMedium)
                        }
                        puzzle.answerError?.let { Text(it, color = colors.error, style = MaterialTheme.typography.bodySmall) }
                    }
                }

                if (challenge.numericOnly && !solved) {
                    Column(Modifier.padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "⌫", "0", "✓").chunked(3).forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                row.forEach { key ->
                                    AppButton(
                                        text = key,
                                        onClick = {
                                            when (key) {
                                                "✓" -> checkCode()
                                                "⌫" -> game.updateAnswer(answer.dropLast(1))
                                                else -> game.updateAnswer(answer + key)
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        style = if (key == "✓") AppButtonStyle.Primary else AppButtonStyle.Outlined,
                                        enabled = !puzzle.submitting,
                                    )
                                }
                            }
                        }
                    }
                }
                if (solved) {
                    AppButton("Вернуться к делу", onBack, style = AppButtonStyle.Accent)
                } else {
                    MorseAlphabetPanel()
                    AppButton(
                        if (puzzle.submitting) "Проверяем ответ…" else "Проверить ответ",
                        ::checkCode,
                        style = AppButtonStyle.Accent,
                        enabled = !puzzle.submitting,
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
    if (showVibrationOptions && !solved) {
        val dismiss = {
            if (!vibration.saving) {
                if (vibration.enabled == null && !vibration.loading) viewModel.setEnabled(false)
                showVibrationOptions = false
            }
        }
        AlertDialog(
            onDismissRequest = dismiss,
            title = { Text("Как получать сигнал Морзе") },
            text = { VibrationSettingRow(vibration, viewModel::setEnabled, viewModel::load) },
            confirmButton = { AppButton("Продолжить", dismiss, enabled = !vibration.saving && !vibration.loading) },
            containerColor = colors.bg,
        )
    }
}

@Composable
internal fun DeviceSettingsScreen(viewModel: VibrationViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = AppTheme.colors
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        CaseToolHeader("Настройки", onBack)
        Text("Возможности устройства", style = MaterialTheme.typography.headlineLarge, color = colors.text)
        AppCard(outlined = true) { VibrationSettingRow(state, viewModel::setEnabled, viewModel::load) }
    }
}

@Composable
private fun VibrationSettingRow(state: VibrationState, onChange: (Boolean) -> Unit, onRetry: () -> Unit) {
    val colors = AppTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Вибрация для Морзе", style = MaterialTheme.typography.titleMedium)
                Text(
                    if (state.available) "Телефон передаёт точки и тире вибрацией. Если выключить, сигнал будет показан на экране."
                    else "На устройстве нет доступного вибромотора. Сигнал будет показан на экране.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.text2,
                )
            }
            Switch(
                checked = state.enabled == true && state.available,
                onCheckedChange = onChange,
                enabled = state.available && !state.loading && !state.saving,
            )
        }
        state.settingsError?.let { ScreenRetry(it, onRetry) }
    }
}

@Composable
internal fun AccusationScreen(
    viewModel: AccusationViewModel,
    onBack: () -> Unit,
    onStatusChanged: (Int, String) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val selected by viewModel.selectedId.collectAsStateWithLifecycle()
    val colors = AppTheme.colors
    var confirming by rememberSaveable { mutableStateOf(false) }
    val result = state.result

    LaunchedEffect(result) {
        result?.let { onStatusChanged(it.caseId, if (it.isCorrect == true) "solved" else "failed") }
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        CaseToolHeader("Обвинение", onBack)
        Text("Кто виновен?", style = MaterialTheme.typography.displayLarge)
        Text("Выберите одного подозреваемого.", style = MaterialTheme.typography.bodySmall, color = colors.text2)
        when {
            state.loading -> ScreenProgress()
            state.error != null && (state.suspects.isEmpty() || state.closed) -> ScreenRetry(state.error.orEmpty(), viewModel::load)
            state.suspects.isEmpty() -> Text("В деле пока нет подозреваемых.", color = colors.text2)
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f).selectableGroup(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.suspects, key = { it.id }) { suspect ->
                        val chosen = suspect.id == selected
                        Surface(
                            modifier = Modifier.fillMaxWidth().clip(AppShapes.card).selectable(
                                selected = chosen,
                                enabled = !state.submitting && !state.closed,
                                role = Role.RadioButton,
                                onClick = { viewModel.select(suspect.id) },
                            ),
                            color = if (chosen) colors.surface2 else colors.surface,
                            shape = AppShapes.card,
                            border = BorderStroke(if (chosen) 2.dp else 1.dp, if (chosen) colors.accent else colors.outline),
                        ) {
                            Column(Modifier.heightIn(min = 154.dp).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(Modifier.size(40.dp).background(colors.primary, AppShapes.control), contentAlignment = Alignment.Center) {
                                    Text(suspect.name.take(1), color = colors.bg)
                                }
                                Text(suspect.name, style = MaterialTheme.typography.titleMedium)
                                Text(suspect.profile, style = MaterialTheme.typography.bodySmall, color = colors.text2, maxLines = 3, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
                state.error?.let { Text(it, color = colors.error) }
                AppButton(
                    text = if (state.submitting) "Проверяем обвинение…" else "Обвинить",
                    onClick = { confirming = true },
                    enabled = state.suspects.any { it.id == selected } && !state.submitting && !state.closed,
                    style = AppButtonStyle.Accent,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
            }
        }
    }

    val chosen = state.suspects.firstOrNull { it.id == selected }
    if (confirming && chosen != null && result == null && !state.closed) {
        AlertDialog(
            onDismissRequest = { confirming = false },
            title = { Text("Подтвердить обвинение") },
            text = { Text("Вы обвиняете персонажа «${chosen.name}». После отправки выбор станет окончательным.") },
            confirmButton = { AppButton("Обвинить", { confirming = false; viewModel.accuse() }, style = AppButtonStyle.Accent) },
            dismissButton = { AppButton("Отмена", { confirming = false }, style = AppButtonStyle.Outlined) },
            containerColor = colors.bg,
        )
    }
    if (result != null) {
        val accused = state.suspects.firstOrNull { it.id == result.suspectId }
        val culprit = state.suspects.firstOrNull { it.id == result.culpritId }
            ?: state.suspects.singleOrNull { result.explanation == "Это ошибка. Преступником был ${it.name}." }
        val showAccused = result.isCorrect != true && accused != null
        val summary = if (result.culpritTruth == null) result.explanation
        else if (result.isCorrect == true) "Вы правильно определили виновного."
        else "Подозреваемый, которого вы выбрали, не совершал это преступление."
        AlertDialog(
            onDismissRequest = onBack,
            title = { Text(if (result.isCorrect == true) "Дело раскрыто" else "Обвинение ошибочно") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TypewriterText(summary, style = MaterialTheme.typography.bodyMedium)
                    if (showAccused && accused != null) {
                        VerdictCard("Кого вы обвинили", accused, result.selectedTruth, delayMillis = 280)
                    }
                    if (culprit != null) {
                        VerdictCard(
                            "Настоящий виновник",
                            culprit,
                            result.culpritTruth,
                            delayMillis = if (showAccused) 650 else 280,
                        )
                    }
                }
            },
            confirmButton = { AppButton("Вернуться к делу", onBack) },
            containerColor = colors.bg,
        )
    }
}

@Composable
private fun VerdictCard(label: String, suspect: Suspect, truth: String?, delayMillis: Long) {
    val colors = AppTheme.colors
    var visible by rememberSaveable(suspect.id, label) { mutableStateOf(false) }
    LaunchedEffect(suspect.id, label) {
        if (!visible) {
            delay(delayMillis)
            visible = true
        }
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(380)) + slideInVertically(tween(380)) { it / 2 },
    ) {
        AppCard(outlined = true) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(label, style = MaterialTheme.typography.bodySmall, color = colors.text2)
                Text(suspect.name, style = MaterialTheme.typography.headlineMedium)
                Text(suspect.profile, style = MaterialTheme.typography.bodySmall, color = colors.text2)
                truth?.takeIf { it.isNotBlank() }?.let {
                    TypewriterText(it, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
