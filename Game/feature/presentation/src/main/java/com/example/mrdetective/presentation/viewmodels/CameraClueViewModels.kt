package com.example.mrdetective.presentation.ui

import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.mrdetective.domain.hardware.ClueCamera
import com.example.mrdetective.domain.hardware.ClueCameraState
import com.example.mrdetective.domain.models.CameraChallenge
import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.repositories.CameraChallengeRepository
import com.example.mrdetective.domain.repositories.CameraSubmission
import com.example.mrdetective.domain.repositories.SceneClueImage
import com.example.mrdetective.domain.repositories.SceneClueRepository
import com.example.mrdetective.domain.usecases.clues.CaptureCameraClueUseCase
import com.example.mrdetective.domain.usecases.clues.ChooseClueChallengeUseCase
import com.example.mrdetective.domain.usecases.clues.ClueChallengeChoice
import com.example.mrdetective.domain.usecases.clues.ClueFramePosition
import com.example.mrdetective.domain.usecases.clues.buildClueSearchScene
import com.example.mrdetective.domain.usecases.clues.chooseClueAzimuth
import com.example.mrdetective.domain.usecases.clues.cluePositionInFrame
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject
import kotlin.random.Random
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

@Serializable
internal data class ChooseClueChallengeRoute(val caseId: Int)

@Serializable
internal data class CameraClueRoute(val caseId: Int, val puzzleId: Int)

internal data class ClueChoiceState(
    val loading: Boolean = true,
    val choice: ClueChallengeChoice? = null,
    val error: String? = null,
)

@HiltViewModel
internal class ClueChallengePickerViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val chooseChallenge: ChooseClueChallengeUseCase,
) : ViewModel() {
    val caseId = savedState.toRoute<ChooseClueChallengeRoute>().caseId
    private val mutableState = MutableStateFlow(ClueChoiceState())
    val state = mutableState.asStateFlow()
    private var loadJob: Job? = null

    init {
        load()
    }

    fun load() {
        if (loadJob?.isActive == true) return
        mutableState.value = ClueChoiceState()
        loadJob = viewModelScope.launch {
            mutableState.value = chooseChallenge(caseId).fold(
                { ClueChoiceState(loading = false, choice = it) },
                { ClueChoiceState(loading = false, error = it.challengeLoadMessage()) },
            )
        }
    }
}

internal data class CameraClueUiState(
    val loading: Boolean = true,
    val challenge: CameraChallenge? = null,
    val capturing: Boolean = false,
    val submission: CameraSubmission? = null,
    val loadMessage: String? = null,
    val captureMessage: String? = null,
    val sceneItems: List<SceneClueImage>? = null,
    val sceneMessage: String? = null,
)

@HiltViewModel
internal class CameraClueViewModel @Inject constructor(
    private val savedState: SavedStateHandle,
    private val repository: CameraChallengeRepository,
    private val camera: ClueCamera<Pair<LifecycleOwner, PreviewView>>,
    private val captureClue: CaptureCameraClueUseCase,
    private val scene: SceneClueRepository,
) : ViewModel() {
    private val route = savedState.toRoute<CameraClueRoute>()
    private val mutableState = MutableStateFlow(CameraClueUiState())
    val state = mutableState.asStateFlow()
    val searchingScene = savedState.getStateFlow("searchingScene", false)
    private val cameraState = MutableStateFlow(ClueCameraState())
    val hardware = cameraState.asStateFlow()
    val targetAzimuth = savedState.getStateFlow<Float?>("cameraTargetAzimuth", null)
    private val mutablePosition = MutableStateFlow<ClueFramePosition?>(null)
    val position = mutablePosition.asStateFlow()
    private var loadJob: Job? = null
    private var observation: Job? = null
    private var sceneLoad: Job? = null

    init {
        load()
    }

    fun load() {
        if (loadJob?.isActive == true || state.value.capturing) return
        observation?.cancel()
        mutableState.value = CameraClueUiState()
        loadJob = viewModelScope.launch {
            repository.load(route.caseId).fold(
                { options ->
                    val challenge = options.firstOrNull { it.id == route.puzzleId }
                    mutableState.value = CameraClueUiState(
                        loading = false,
                        challenge = challenge,
                        submission = when {
                            challenge?.rewardClaimed == true -> CameraSubmission.Awarded
                            challenge?.pendingOrCompleted == true -> CameraSubmission.Pending
                            else -> null
                        },
                        loadMessage = if (challenge == null) "Эта загадка больше недоступна." else null,
                    )
                    if (state.value.submission == CameraSubmission.Pending && challenge != null) observe(challenge)
                },
                {
                    mutableState.value = CameraClueUiState(loading = false, loadMessage = it.challengeLoadMessage())
                },
            )
        }
    }

    fun useScene(enabled: Boolean) {
        savedState["searchingScene"] = enabled
    }

    fun loadScene() {
        val current = state.value
        val challenge = current.challenge ?: return
        if (sceneLoad?.isActive == true || current.sceneItems != null) return
        sceneLoad = viewModelScope.launch {
            val decoys = scene.loadDecoys(challenge.clueId)
            val items = buildClueSearchScene(SceneClueImage(challenge.clueId, challenge.imageUrl), decoys, Random(challenge.id))
            mutableState.update { it.copy(sceneItems = items) }
        }
    }

    fun selectSceneItem(index: Int) {
        val current = state.value
        val challenge = current.challenge ?: return
        val selected = current.sceneItems?.getOrNull(index) ?: return
        if (current.capturing || current.submission in setOf(CameraSubmission.Pending, CameraSubmission.Awarded)) return
        if (selected.clueId != challenge.clueId) {
            mutableState.update { it.copy(sceneMessage = "Это другой предмет. Продолжайте поиск.") }
            return
        }

        mutableState.update { it.copy(capturing = true, sceneMessage = null) }
        viewModelScope.launch {
            scene.submit(challenge, challenge.clueId).fold(
                onSuccess = { mutableState.update { it.copy(capturing = false, submission = CameraSubmission.Awarded) } },
                onFailure = { error ->
                    if (error is CancellationException) throw error
                    val message = if (error is DomainError.Network) {
                        "Подключитесь к интернету и нажмите на улику ещё раз."
                    } else {
                        "Не удалось подтвердить улику. Попробуйте ещё раз."
                    }
                    mutableState.update { it.copy(capturing = false, sceneMessage = message) }
                },
            )
        }
    }

    fun attach(owner: LifecycleOwner, preview: PreviewView): AutoCloseable {
        val session = camera.attach(owner to preview) { update ->
            if (targetAzimuth.value == null && update.previewReady && update.error == null) {
                update.direction?.let(::chooseClueAzimuth)?.let { savedState["cameraTargetAzimuth"] = it }
            }
            val target = targetAzimuth.value
            mutablePosition.value = if (target != null && update.previewReady && update.error == null) {
                update.direction?.let { cluePositionInFrame(target, it, alreadyVisible = position.value != null) }
            } else {
                null
            }
            cameraState.value = update
        }
        return AutoCloseable {
            session.close()
            mutablePosition.value = null
            cameraState.value = ClueCameraState()
        }
    }

    fun capture() {
        val current = state.value
        val challenge = current.challenge ?: return
        val target = targetAzimuth.value ?: return
        val currentCamera = hardware.value
        val direction = currentCamera.direction ?: return
        if (!currentCamera.previewReady || currentCamera.error != null || current.capturing ||
            current.submission == CameraSubmission.Awarded || current.submission == CameraSubmission.Pending
        ) return
        if (position.value == null) {
            mutableState.update { it.copy(captureMessage = "Улика не попала в кадр. Продолжайте поиск.") }
            return
        }
        mutableState.update { it.copy(capturing = true, captureMessage = null, submission = null) }
        viewModelScope.launch {
            try {
                val path = captureClue(target, direction, camera)
                if (path == null) {
                    mutableState.update { it.copy(captureMessage = "Улика не попала в кадр. Продолжайте поиск.") }
                    return@launch
                }
                withContext(NonCancellable) {
                    repository.submit(challenge, path).getOrElse {
                        File(path).delete()
                        throw it
                    }
                }
                mutableState.update { it.copy(submission = CameraSubmission.Pending) }
                observe(challenge)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutableState.update { it.copy(captureMessage = "Не удалось сохранить снимок. Попробуйте ещё раз.") }
            } finally {
                mutableState.update { it.copy(capturing = false) }
            }
        }
    }

    private fun observe(challenge: CameraChallenge) {
        observation?.cancel()
        observation = viewModelScope.launch {
            repository.observeSubmission(challenge).collect { submission ->
                mutableState.update { it.copy(submission = submission) }
            }
        }
    }
}

private fun Throwable.challengeLoadMessage(): String = when (this) {
    is DomainError.Validation -> message ?: "Эта загадка пока недоступна."
    is DomainError.Network -> "Не удалось загрузить загадки. Проверьте подключение."
    else -> "Не удалось загрузить загадки. Попробуйте ещё раз."
}
