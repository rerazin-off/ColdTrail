package com.example.mrdetective.presentation.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.mrdetective.domain.models.CameraChallenge
import com.example.mrdetective.domain.repositories.CameraSubmission
import com.example.mrdetective.domain.usecases.clues.ClueChallengeChoice
import com.example.mrdetective.domain.usecases.clues.cluePositionInFrame
import com.example.mrdetective.presentation.R
import com.example.mrdetective.presentation.ui.components.AppButton
import com.example.mrdetective.presentation.ui.components.AppButtonStyle
import com.example.mrdetective.presentation.ui.theme.AppShapes
import com.example.mrdetective.presentation.ui.theme.AppTheme

@Composable
internal fun ChooseClueChallengeScreen(
    viewModel: ClueChallengePickerViewModel,
    onChoice: (ClueChallengeChoice) -> Unit,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.choice) {
        state.choice?.let(onChoice)
    }
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        CaseToolHeader("Добыть улику", onBack)
        when {
            state.loading || state.choice != null -> CameraClueLoading("Ищем доступную загадку…")
            state.error != null -> CameraClueMessage(state.error.orEmpty(), "Повторить", viewModel::load)
            else -> CameraClueMessage("В этом деле больше нет доступных загадок.", "К делу", onBack)
        }
    }
}

@Composable
internal fun CameraClueScreen(viewModel: CameraClueViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    fun hasPermission() =
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    var permitted by remember(context) { mutableStateOf(hasPermission()) }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { permitted = it }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { permitted = hasPermission() }

    Column(
        Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val challenge = state.challenge
        CaseToolHeader(challenge?.title ?: "Поиск улики", onBack)
        when {
            state.loading -> CameraClueLoading("Загружаем улику…")
            challenge == null -> CameraClueMessage(state.loadMessage.orEmpty(), "Повторить", viewModel::load)
            state.submission == CameraSubmission.Awarded ->
                CameraClueMessage("Улика «${challenge.clueTitle}» добавлена в инвентарь.", "К делу", onBack)
            state.submission == CameraSubmission.Pending ->
                CameraClueMessage("Снимок сохранён. Улика появится в инвентаре после отправки снимка.", "К делу", onBack)
            !permitted -> CameraClueMessage(
                "Разрешите доступ к камере, чтобы осмотреть комнату и сфотографировать улику.",
                "Разрешить доступ",
            ) { permission.launch(Manifest.permission.CAMERA) }
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

    val position = targetAzimuth?.let { target ->
        hardware.direction?.takeIf { hardware.previewReady && hardware.error == null }
            ?.let { cluePositionInFrame(target, it) }
    }
    val instruction = when {
        hardware.error != null -> hardware.error.orEmpty()
        state.submission == CameraSubmission.Failed -> "Не удалось подтвердить снимок. Сфотографируйте улику ещё раз."
        state.captureMessage != null && position == null -> state.captureMessage
        state.captureMessage != null && state.captureMessage.startsWith("Не удалось") -> state.captureMessage
        position != null -> "Улика в кадре. Сделайте снимок."
        !hardware.previewReady -> "Открываем камеру…"
        hardware.direction == null || targetAzimuth == null -> "Держите телефон перед собой, чтобы начать поиск."
        else -> challenge.hint?.takeIf { it.isNotBlank() }
            ?: "Держите телефон перед собой и медленно поворачивайтесь."
    }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Keep the viewfinder size stable when the instruction changes.
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
            if (position != null) {
                val size = minOf(maxWidth, maxHeight) * 0.30f
                val placeholder = painterResource(R.drawable.ic_camera_clue_placeholder)
                AsyncImage(
                    model = challenge.imageUrl,
                    contentDescription = "Улика в кадре",
                    placeholder = placeholder,
                    fallback = placeholder,
                    error = placeholder,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.align(Alignment.TopStart)
                        .offset(maxWidth * position.x - size / 2, maxHeight * position.y - size / 2)
                        .size(size),
                )
            }
        }
        AppButton(
            if (state.capturing) "Сохраняем снимок…" else "Сфотографировать",
            viewModel::capture,
            style = AppButtonStyle.Accent,
            enabled = hardware.previewReady && hardware.direction != null && targetAzimuth != null &&
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
private fun CameraClueMessage(message: String, action: String, onAction: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(message, style = MaterialTheme.typography.bodyLarge)
        AppButton(action, onAction, style = AppButtonStyle.Accent)
    }
}
