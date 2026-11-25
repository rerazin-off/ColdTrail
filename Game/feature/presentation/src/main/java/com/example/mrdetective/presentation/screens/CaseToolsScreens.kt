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
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mrdetective.domain.models.Suspect
import com.example.mrdetective.presentation.R
import com.example.mrdetective.presentation.ui.components.AppButton
import com.example.mrdetective.presentation.ui.components.AppButtonStyle
import com.example.mrdetective.presentation.ui.components.AppCard
import com.example.mrdetective.presentation.ui.components.AppTextField
import com.example.mrdetective.presentation.ui.components.CameraAccess
import com.example.mrdetective.presentation.ui.components.ClueCard
import com.example.mrdetective.presentation.ui.components.ClueRewardDialog
import com.example.mrdetective.presentation.ui.components.ErrorNotice
import com.example.mrdetective.presentation.ui.components.MorseAlphabetPanel
import com.example.mrdetective.presentation.ui.components.NumericPuzzleKeypad
import com.example.mrdetective.presentation.ui.components.NumericPuzzleSlots
import com.example.mrdetective.presentation.ui.components.TypewriterText
import com.example.mrdetective.presentation.ui.components.rememberCameraAccess
import com.example.mrdetective.presentation.ui.theme.AppShapes
import com.example.mrdetective.presentation.ui.theme.AppTheme
import kotlinx.coroutines.delay

@Composable
internal fun CaseToolHeader(
    title: String,
    onBack: () -> Unit,
    actions: @Composable () -> Unit = {},
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onBack) {
            Icon(painterResource(R.drawable.ic_chat_back), stringResource(R.string.back), tint = AppTheme.colors.text)
        }
        Text(title, Modifier.weight(1f).semantics { heading() }, style = MaterialTheme.typography.headlineMedium)
        actions()
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
    val nickname by viewModel.nickname.collectAsStateWithLifecycle()
    val nicknameError by viewModel.nicknameError.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.load()
        viewModel.loadNickname()
    }
    val colors = AppTheme.colors
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CaseToolHeader(stringResource(R.string.profile), onBack) {
            IconButton(onSettings) {
                Icon(painterResource(R.drawable.ic_settings), stringResource(R.string.settings), tint = colors.text)
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
                Text(nickname?.take(2)?.uppercase().orEmpty(), style = MaterialTheme.typography.displayLarge, color = colors.accent)
            }
        }
        Spacer(Modifier.height(20.dp))
        nickname?.let { Text(it, style = MaterialTheme.typography.headlineLarge) }
        if (auth.message != null) {
            ErrorNotice(auth.message, onSignOut)
        } else {
            ErrorNotice(nicknameError ?: (statisticsState as? ProfileStatisticsState.Error)?.message) {
                viewModel.loadNickname()
                viewModel.load()
            }
        }
        Text(stringResource(R.string.profile_progress), style = MaterialTheme.typography.bodySmall, color = colors.text2)
        Spacer(Modifier.height(32.dp))
        when (val current = statisticsState) {
            ProfileStatisticsState.Loading -> CircularProgressIndicator(color = colors.accent)
            is ProfileStatisticsState.Error -> Unit
            is ProfileStatisticsState.Content ->
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    maxItemsInEachRow = if (LocalDensity.current.fontScale >= 1.4f) 1 else 3,
                ) {
                    val stats = current.statistics
                    listOf(
                        stats.total to stringResource(R.string.total_cases),
                        stats.active to stringResource(R.string.active_total),
                        stats.solved to stringResource(R.string.solved_total),
                    ).forEach {
                            (value, label) ->
                        AppCard(Modifier.weight(1f), outlined = true, padding = 8.dp) {
                            Column(
                                Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(value.toString(), color = colors.text, style = MaterialTheme.typography.headlineMedium)
                                Text(label, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
        }
        Spacer(Modifier.height(24.dp))
        AppButton(
            text = if (auth.busy) stringResource(R.string.signing_out) else stringResource(R.string.sign_out),
            onClick = onSignOut,
            enabled = !auth.busy,
            style = AppButtonStyle.Outlined,
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
internal fun InventoryScreen(
    viewModel: InventoryViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = AppTheme.colors
    var selectedClueId by rememberSaveable { mutableStateOf<Int?>(null) }
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        CaseToolHeader(stringResource(R.string.clue_inventory), onBack)
        Text(stringResource(R.string.case_materials), style = MaterialTheme.typography.displayLarge)
        Text(stringResource(R.string.inventory_intro), color = colors.text2, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(20.dp))
        when (val current = state) {
            CluePickerState.Loading -> ScreenProgress()
            CluePickerState.Error -> ScreenRetry(stringResource(R.string.inventory_load_error), viewModel::load)
            is CluePickerState.Content ->
                if (current.clues.isEmpty()) {
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        AppCard(outlined = true) {
                            Column(
                                Modifier.fillMaxWidth().padding(vertical = 36.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Icon(painterResource(R.drawable.ic_inventory), null, tint = colors.accent)
                                Text(stringResource(R.string.no_clues), style = MaterialTheme.typography.headlineLarge)
                                Text(
                                    stringResource(R.string.no_clues_hint),
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center,
                                    color = colors.text2,
                                )
                            }
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(if (LocalDensity.current.fontScale >= 1.4f) 280.dp else 170.dp),
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
                                    Text(
                                        clue.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        clue.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.text2,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }
                    current.clues.firstOrNull { it.id == selectedClueId }?.let { clue ->
                        AlertDialog(
                            onDismissRequest = { selectedClueId = null },
                            title = { Text(stringResource(R.string.case_item)) },
                            text = { ClueCard(clue.title, clue.description) },
                            confirmButton = { AppButton(stringResource(R.string.close), { selectedClueId = null }) },
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

    val colors = AppTheme.colors
    val solved = puzzle.solved
    if (solved) {
        ClueRewardDialog(puzzle.reward?.title, puzzle.reward?.description, onBack)
    }

    fun checkCode() {
        viewModel.stopPlayback()
        game.submit()
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CaseToolHeader(stringResource(R.string.puzzle), onBack) {
            IconButton(
                onClick = {
                    viewModel.stopPlayback()
                    showVibrationOptions = true
                },
            ) {
                Icon(painterResource(R.drawable.ic_help), stringResource(R.string.vibration_settings), tint = colors.text)
            }
        }
        when {
            puzzle.loading -> CircularProgressIndicator(color = colors.accent)
            puzzle.loadError != null -> ScreenRetry(puzzle.loadError.orEmpty(), game::load)
            challenge == null ->
                AppCard(outlined = true) {
                    Text(stringResource(R.string.no_morse_puzzles))
                }
            else -> {
                Text(challenge.title, style = MaterialTheme.typography.displayLarge)
                Text(stringResource(R.string.decode_morse), style = MaterialTheme.typography.bodySmall, color = colors.text2)
                Spacer(Modifier.height(12.dp))
                AppCard(outlined = true) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        challenge.hint?.takeIf { it.isNotBlank() }?.let {
                            Text(it, style = MaterialTheme.typography.bodySmall, color = colors.text2)
                        }
                        when {
                            vibration.loading ->
                                Text(
                                    stringResource(R.string.loading_signal_settings),
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            vibration.usesVibration ->
                                AppButton(
                                    if (vibration.playing) stringResource(R.string.playing_signal) else stringResource(R.string.play_code),
                                    { viewModel.play(challenge.signal) },
                                    style = AppButtonStyle.Outlined,
                                    enabled = !vibration.playing && !vibration.saving && !solved && !puzzle.submitting,
                                )
                            else ->
                                Text(
                                    challenge.signal.replace(" ", " / "),
                                    style = MaterialTheme.typography.headlineMedium,
                                )
                        }
                        ErrorNotice(vibration.playbackError)
                        if (challenge.numericOnly) {
                            NumericPuzzleSlots(challenge.answerLength, answer, solved, puzzle.answerError != null)
                        } else if (!solved) {
                            AppTextField(
                                value = answer,
                                onValueChange = game::updateAnswer,
                                placeholder = stringResource(R.string.morse_answer),
                                enabled = !puzzle.submitting,
                            )
                            Text(
                                stringResource(R.string.answer_length, challenge.answerLength),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.text2,
                            )
                        }
                        if (solved) {
                            Text(stringResource(R.string.clue_added), color = colors.success, style = MaterialTheme.typography.bodyMedium)
                        }
                        ErrorNotice(
                            puzzle.answerError,
                            when {
                                puzzle.retrySync -> game::retrySync
                                puzzle.retryAnswer -> ::checkCode
                                else -> null
                            },
                        )
                    }
                }

                if (challenge.numericOnly && !solved) {
                    NumericPuzzleKeypad(
                        onDigit = { game.updateAnswer(game.answer.value + it) },
                        onDelete = { game.updateAnswer(game.answer.value.dropLast(1)) },
                        onSubmit = ::checkCode,
                        enabled = !puzzle.submitting,
                    )
                }
                if (solved) {
                    AppButton(stringResource(R.string.return_to_case), onBack, style = AppButtonStyle.Accent)
                } else {
                    MorseAlphabetPanel()
                    AppButton(
                        if (puzzle.submitting) stringResource(R.string.checking_answer) else stringResource(R.string.check_answer),
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
            title = { Text(stringResource(R.string.morse_signal_options)) },
            text = { VibrationSettingRow(vibration, viewModel::setEnabled, viewModel::load) },
            confirmButton = {
                AppButton(
                    stringResource(R.string.continue_action),
                    dismiss,
                    enabled = !vibration.saving && !vibration.loading,
                )
            },
            containerColor = colors.bg,
        )
    }
}

@Composable
internal fun DeviceSettingsScreen(
    viewModel: VibrationViewModel,
    cameraSettings: CameraSettingsViewModel,
    onBack: () -> Unit,
) {
    val access = rememberCameraAccess(cameraSettings::setEnabled)
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        CaseToolHeader(stringResource(R.string.settings), onBack)
        Text(stringResource(R.string.device_features), style = MaterialTheme.typography.headlineLarge, color = AppTheme.colors.text)
        DevicePermissionOptions(viewModel, cameraSettings, access)
    }
}

@Composable
internal fun DevicePermissionOptions(
    viewModel: VibrationViewModel,
    cameraSettings: CameraSettingsViewModel,
    access: CameraAccess,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val camera by cameraSettings.state.collectAsStateWithLifecycle()
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AppCard(outlined = true) { VibrationSettingRow(state, viewModel::setEnabled, viewModel::load) }
        AppCard(outlined = true) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DeviceSwitchRow(
                    title = stringResource(R.string.clue_camera),
                    description =
                        if (access.available) {
                            stringResource(R.string.camera_setting_summary)
                        } else {
                            stringResource(R.string.rear_camera_missing)
                        },
                    checked = access.available && access.granted && camera.enabled != false && !camera.loading,
                    enabled = access.available && !camera.loading && !camera.saving && camera.error == null,
                    onChange = { enabled ->
                        if (enabled) {
                            access.requestOrOpenSettings(camera.enabled != null)
                        } else {
                            cameraSettings.setEnabled(false)
                        }
                    },
                )
                if (access.available) {
                    TextButton(onClick = { access.openSettings(!access.granted) }, enabled = !camera.saving) {
                        Text(stringResource(R.string.android_permission))
                    }
                }
                camera.error?.let { ScreenRetry(it, cameraSettings::load) }
            }
        }
    }
}

@Composable
private fun VibrationSettingRow(
    state: VibrationState,
    onChange: (Boolean) -> Unit,
    onRetry: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        DeviceSwitchRow(
            title = stringResource(R.string.morse_vibration),
            description =
                if (state.available) {
                    stringResource(R.string.vibration_summary)
                } else {
                    stringResource(R.string.vibration_unavailable)
                },
            checked = state.enabled == true && state.available,
            enabled = state.available && !state.loading && !state.saving,
            onChange = onChange,
        )
        state.settingsError?.let { ScreenRetry(it, onRetry) }
    }
}

@Composable
private fun DeviceSwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    enabled: Boolean,
    onChange: (Boolean) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(description, style = MaterialTheme.typography.bodySmall, color = AppTheme.colors.text2)
        }
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            enabled = enabled,
            modifier = Modifier.semantics { contentDescription = title },
        )
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
        CaseToolHeader(stringResource(R.string.accusation), onBack)
        Text(stringResource(R.string.who_did_it), style = MaterialTheme.typography.displayLarge)
        Text(stringResource(R.string.choose_one_suspect), style = MaterialTheme.typography.bodySmall, color = colors.text2)
        when {
            state.loading -> ScreenProgress()
            state.error != null && (state.suspects.isEmpty() || state.closed) -> ScreenRetry(state.error.orEmpty(), viewModel::load)
            state.suspects.isEmpty() -> Text(stringResource(R.string.no_suspects_for_case), color = colors.text2)
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(if (LocalDensity.current.fontScale >= 1.4f) 280.dp else 170.dp),
                    modifier = Modifier.weight(1f).selectableGroup(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.suspects, key = { it.id }) { suspect ->
                        val chosen = suspect.id == selected
                        Surface(
                            modifier =
                                Modifier.fillMaxWidth().clip(AppShapes.card).selectable(
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
                                Box(
                                    Modifier.size(40.dp).background(colors.primary, AppShapes.control),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(suspect.name.take(1), color = colors.onPrimary)
                                }
                                Text(suspect.name, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    suspect.profile,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.text2,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
                ErrorNotice(state.error, viewModel::accuse)
                AppButton(
                    text = if (state.submitting) stringResource(R.string.checking_accusation) else stringResource(R.string.accuse),
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
            title = { Text(stringResource(R.string.confirm_accusation)) },
            text = { Text(stringResource(R.string.accusation_confirmation, chosen.name)) },
            confirmButton = {
                AppButton(stringResource(R.string.accuse), {
                    confirming = false
                    viewModel.accuse()
                }, style = AppButtonStyle.Accent)
            },
            dismissButton = { AppButton(stringResource(R.string.cancel), { confirming = false }, style = AppButtonStyle.Outlined) },
            containerColor = colors.bg,
        )
    }
    if (result != null) {
        val accused = state.suspects.firstOrNull { it.id == result.suspectId }
        val culprit =
            state.suspects.firstOrNull { it.id == result.culpritId }
                ?: state.suspects.singleOrNull { result.explanation == "Это ошибка. Преступником был ${it.name}." }
        val showAccused = result.isCorrect != true && accused != null
        val summary =
            if (result.culpritTruth == null) {
                result.explanation
            } else if (result.isCorrect == true) {
                stringResource(R.string.culprit_correct)
            } else {
                stringResource(R.string.culprit_incorrect)
            }
        AlertDialog(
            onDismissRequest = onBack,
            title = {
                Text(
                    if (result.isCorrect == true) stringResource(R.string.case_solved) else stringResource(R.string.accusation_incorrect),
                )
            },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TypewriterText(summary, style = MaterialTheme.typography.bodyMedium)
                    if (showAccused) {
                        VerdictCard(stringResource(R.string.accused_person), accused, result.selectedTruth, delayMillis = 280)
                    }
                    if (culprit != null) {
                        VerdictCard(
                            stringResource(R.string.actual_culprit),
                            culprit,
                            result.culpritTruth,
                            delayMillis = if (showAccused) 650 else 280,
                        )
                    }
                }
            },
            confirmButton = { AppButton(stringResource(R.string.return_to_case), onBack) },
            containerColor = colors.bg,
        )
    }
}

@Composable
private fun VerdictCard(
    label: String,
    suspect: Suspect,
    truth: String?,
    delayMillis: Long,
) {
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
