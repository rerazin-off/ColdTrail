package com.example.mrdetective.presentation.ui

import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.mrdetective.domain.hardware.ClueSearchMode
import com.example.mrdetective.domain.hardware.chooseClueSearchMode
import com.example.mrdetective.domain.models.CameraChallenge
import com.example.mrdetective.domain.repositories.CameraSubmission
import com.example.mrdetective.domain.usecases.clues.ClueChallengeChoice
import com.example.mrdetective.presentation.R
import com.example.mrdetective.presentation.ui.components.AppButton
import com.example.mrdetective.presentation.ui.components.AppButtonStyle
import com.example.mrdetective.presentation.ui.components.ClueRewardDialog
import com.example.mrdetective.presentation.ui.components.ErrorNotice
import com.example.mrdetective.presentation.ui.components.rememberCameraAccess
import com.example.mrdetective.presentation.ui.theme.AppShapes
import com.example.mrdetective.presentation.ui.theme.AppTheme
import kotlin.math.roundToInt

@Composable
internal fun ChooseClueChallengeScreen(
    viewModel: ClueChallengePickerViewModel,
    vibration: VibrationViewModel,
    cameraSettings: CameraSettingsViewModel,
    onChoice: (ClueChallengeChoice) -> Unit,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val vibrationState by vibration.state.collectAsStateWithLifecycle()
    val cameraState by cameraSettings.state.collectAsStateWithLifecycle()
    val access = rememberCameraAccess(cameraSettings::setEnabled)
    LaunchedEffect(state.choice) {
        state.choice?.let(onChoice)
    }
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        CaseToolHeader(stringResource(R.string.find_clue), onBack)
        when {
            state.loading || state.choice != null -> CameraClueLoading(stringResource(R.string.looking_for_puzzle))
            state.error != null -> ScreenRetry(state.error.orEmpty(), viewModel::load)
            state.permissionsRequired ->
                AlertDialog(
                    onDismissRequest = onBack,
                    title = { Text(stringResource(R.string.camera_and_vibration)) },
                    text = {
                        Column(Modifier.verticalScroll(rememberScrollState())) {
                            DevicePermissionOptions(vibration, cameraSettings, access)
                        }
                    },
                    confirmButton = {
                        AppButton(
                            stringResource(R.string.continue_action),
                            { viewModel.continueWithPermissions(access.granted) },
                            enabled = !cameraState.loading && !cameraState.saving && !vibrationState.loading && !vibrationState.saving,
                        )
                    },
                    containerColor = AppTheme.colors.surface,
                    titleContentColor = AppTheme.colors.text,
                    textContentColor = AppTheme.colors.text,
                )
            else -> CameraClueMessage(stringResource(R.string.no_case_puzzles), stringResource(R.string.back_to_case), onBack)
        }
    }
}

@Composable
internal fun CameraClueScreen(
    viewModel: CameraClueViewModel,
    cameraSettings: CameraSettingsViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val settings by cameraSettings.state.collectAsStateWithLifecycle()
    val searchingScene by viewModel.searchingScene.collectAsStateWithLifecycle()
    val hardware by viewModel.hardware.collectAsStateWithLifecycle()
    val access =
        rememberCameraAccess { granted ->
            cameraSettings.setEnabled(granted)
            viewModel.useScene(!granted)
        }
    val searchMode = chooseClueSearchMode(settings.enabled, access.available, access.granted, access.showRationale)
    LaunchedEffect(hardware.error) {
        hardware.error?.let(viewModel::fallbackAfterCameraError)
    }

    Column(
        Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val challenge = state.challenge
        CaseToolHeader(challenge?.title ?: stringResource(R.string.clue_search), onBack)
        when {
            state.loading -> CameraClueLoading(stringResource(R.string.loading_clue))
            challenge == null -> ScreenRetry(state.loadMessage.orEmpty(), viewModel::load)
            state.submission == CameraSubmission.Awarded ->
                ClueRewardDialog(challenge.clueTitle, challenge.clueDescription, onBack)
            state.submission == CameraSubmission.Pending -> {
                val message = if (searchingScene || searchMode == ClueSearchMode.Picture) R.string.sync_pending else R.string.photo_queued
                CameraClueMessage(stringResource(message), stringResource(R.string.back_to_case), onBack)
                ErrorNotice(state.syncError, viewModel::retrySync)
            }
            settings.loading || settings.saving -> CameraClueLoading(stringResource(R.string.loading_camera_settings))
            searchingScene || searchMode == ClueSearchMode.Picture ->
                SceneClueSearch(
                    challenge = challenge,
                    state = state,
                    onLoad = viewModel::loadScene,
                    onSelect = viewModel::selectSceneItem,
                    onCameraSettings =
                        if (access.available && !access.granted) {
                            { access.openSettings(true) }
                        } else {
                            null
                        },
                )
            searchMode == ClueSearchMode.AskPermission ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.camera_permission_rationale))
                    AppButton(stringResource(R.string.allow_access), { access.requestOrOpenSettings(false) }, style = AppButtonStyle.Accent)
                    AppButton(stringResource(R.string.search_picture), { viewModel.useScene(true) }, style = AppButtonStyle.Outlined)
                }
            else -> CameraViewfinder(challenge, state, viewModel, Modifier.weight(1f))
        }
    }
}

@Composable
private fun CameraViewfinder(
    challenge: CameraChallenge,
    state: CameraClueUiState,
    viewModel: CameraClueViewModel,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val hardware by viewModel.hardware.collectAsStateWithLifecycle()
    val targetAzimuth by viewModel.targetAzimuth.collectAsStateWithLifecycle()
    val preview = remember(context) { PreviewView(context) }
    val colors = AppTheme.colors
    DisposableEffect(owner, preview, viewModel) {
        val attachment = viewModel.attach(owner, preview)
        onDispose { attachment.close() }
    }

    val position by viewModel.position.collectAsStateWithLifecycle()
    val visiblePosition = position
    val instruction =
        when {
            hardware.error != null -> hardware.error.orEmpty()

            position != null -> stringResource(R.string.clue_in_frame_hint)
            !hardware.previewReady -> stringResource(R.string.opening_camera)
            hardware.direction == null || targetAzimuth == null -> stringResource(R.string.hold_camera)
            else ->
                challenge.hint?.takeIf { it.isNotBlank() }
                    ?: stringResource(R.string.turn_camera)
        }

    ErrorNotice(state.captureMessage)
    ErrorNotice(if (state.submission == CameraSubmission.Failed) stringResource(R.string.capture_failed) else null)
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // При смене инструкции размер видоискателя остаётся прежним.
        Text(
            instruction,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.text,
            minLines = 2,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        BoxWithConstraints(
            Modifier.fillMaxWidth().weight(1f)
                .clip(AppShapes.control)
                .background(colors.surface)
                .border(1.dp, colors.outline, AppShapes.control),
            contentAlignment = Alignment.Center,
        ) {
            AndroidView(factory = { preview }, modifier = Modifier.matchParentSize())
            if (!hardware.previewReady && hardware.error == null) {
                CircularProgressIndicator(color = colors.accent)
            }
            if (visiblePosition != null) {
                val x by animateFloatAsState(visiblePosition.x, tween(90, easing = LinearEasing), label = "Clue horizontal position")
                val y by animateFloatAsState(visiblePosition.y, tween(90, easing = LinearEasing), label = "Clue vertical position")
                val width = maxWidth
                val height = maxHeight
                val size = minOf(width, height) * 0.30f
                val placeholder = painterResource(R.drawable.ic_camera_clue_placeholder)
                AsyncImage(
                    model = challenge.imageUrl,
                    contentDescription = stringResource(R.string.clue_in_frame),
                    placeholder = placeholder,
                    fallback = placeholder,
                    error = placeholder,
                    contentScale = ContentScale.Fit,
                    modifier =
                        Modifier.align(Alignment.TopStart)
                            .offset {
                                IntOffset(
                                    (width.toPx() * x - size.toPx() / 2).roundToInt(),
                                    (height.toPx() * y - size.toPx() / 2).roundToInt(),
                                )
                            }
                            .size(size),
                )
            }
        }
        AppButton(
            if (state.capturing) stringResource(R.string.saving_photo) else stringResource(R.string.take_photo),
            viewModel::capture,
            style = AppButtonStyle.Accent,
            enabled =
                hardware.previewReady && hardware.direction != null && targetAzimuth != null &&
                    hardware.error == null && !state.capturing,
        )
    }
}

@Composable
private fun CameraClueLoading(message: String) {
    Column(
        Modifier.fillMaxWidth().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(color = AppTheme.colors.accent)
        Text(message)
    }
}

@Composable
private fun CameraClueMessage(
    message: String,
    action: String,
    onAction: () -> Unit,
) {
    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(message, style = MaterialTheme.typography.bodyLarge)
        AppButton(action, onAction, style = AppButtonStyle.Accent)
    }
}
