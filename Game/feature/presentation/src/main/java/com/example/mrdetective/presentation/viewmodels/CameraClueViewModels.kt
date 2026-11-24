package com.example.mrdetective.presentation.ui

import com.example.mrdetective.presentation.R
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.mrdetective.domain.hardware.CameraPreferences
import com.example.mrdetective.domain.hardware.ClueCamera
import com.example.mrdetective.domain.hardware.ClueCameraState
import com.example.mrdetective.domain.hardware.PuzzlePermissionsPreferences
import com.example.mrdetective.domain.models.CameraChallenge
import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.repositories.AuthRepository
import com.example.mrdetective.domain.repositories.CameraChallengeRepository
import com.example.mrdetective.domain.repositories.CameraSubmission
import com.example.mrdetective.domain.repositories.ProgressRepository
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
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
    val permissionsRequired: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
internal class ClueChallengePickerViewModel @Inject constructor(
    private val strings: UiStrings,
    savedState: SavedStateHandle,
    private val chooseChallenge: ChooseClueChallengeUseCase,
    private val auth: AuthRepository,
    private val permissions: PuzzlePermissionsPreferences,
    private val cameraPreferences: CameraPreferences,
) : ViewModel() {
    val caseId = savedState.toRoute<ChooseClueChallengeRoute>().caseId
    private val mutableState = MutableStateFlow(ClueChoiceState())
    val state = mutableState.asStateFlow()
    private var loadJob: Job? = null

    init {
        load()
    }

    fun load() = prepareChallenge(cameraGranted = null)

    fun continueWithPermissions(cameraGranted: Boolean) = prepareChallenge(cameraGranted)

    private fun prepareChallenge(cameraGranted: Boolean?) {
        if (loadJob?.isActive == true) return
        mutableState.value = ClueChoiceState()
        loadJob = viewModelScope.launch {
            mutableState.value = try {
                val user = auth.currentUser() ?: throw DomainError.Unauthorized()
                if (cameraGranted != null) {
                    if (!cameraGranted && cameraPreferences.cameraEnabled.first() == null) {
                        cameraPreferences.setCameraEnabled(false)
                    }
                    permissions.markPuzzlePermissionsSeen(user)
                }
                if (!permissions.hasSeenPuzzlePermissions(user)) {
                    ClueChoiceState(loading = false, permissionsRequired = true)
                } else {
                    ClueChoiceState(loading = false, choice = chooseChallenge(caseId).getOrThrow())
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                ClueChoiceState(loading = false, error = error.challengeLoadMessage(strings))
            }
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
    val syncError: String? = null,
    val sceneItems: List<SceneClueImage>? = null,
    val sceneMessage: String? = null,
    val sceneRetry: Boolean = false,
)

@HiltViewModel
internal class CameraClueViewModel @Inject constructor(
    private val strings: UiStrings,
    private val savedState: SavedStateHandle,
    private val repository: CameraChallengeRepository,
    private val camera: ClueCamera<Pair<LifecycleOwner, PreviewView>>,
    private val captureClue: CaptureCameraClueUseCase,
    private val scene: SceneClueRepository,
    private val progress: ProgressRepository,
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
                        loadMessage = if (challenge == null) strings.get(R.string.puzzle_unavailable) else null,
                    )
                    if (state.value.submission == CameraSubmission.Pending && challenge != null) observe(challenge)
                },
                {
                    mutableState.value = CameraClueUiState(loading = false, loadMessage = it.challengeLoadMessage(strings))
                },
            )
        }
    }

    fun useScene(enabled: Boolean) {
        savedState["searchingScene"] = enabled
    }

    fun fallbackAfterCameraError(message: String) {
        useScene(true)
        val explanation = strings.domainMessage(message, strings.get(R.string.rear_camera_error))
        mutableState.update { it.copy(sceneMessage = explanation, sceneRetry = false) }
    }

    fun loadScene() {
        val current = state.value
        val challenge = current.challenge ?: return
        if (sceneLoad?.isActive == true || current.sceneItems != null) return
        sceneLoad = viewModelScope.launch {
            val decoys = scene.loadDecoys(challenge.clueId)
            val items = buildClueSearchScene(
                SceneClueImage(challenge.clueId, challenge.imageUrl, challenge.clueTitle),
                decoys,
                Random(challenge.id),
            )
            mutableState.update { it.copy(sceneItems = items) }
        }
    }

    fun selectSceneItem(index: Int) {
        val current = state.value
        val challenge = current.challenge ?: return
        val selected = current.sceneItems?.getOrNull(index) ?: return
        if (current.capturing || current.submission in setOf(CameraSubmission.Pending, CameraSubmission.Awarded)) return
        if (selected.clueId != challenge.clueId) {
            mutableState.update { it.copy(sceneMessage = strings.get(R.string.wrong_warehouse_item), sceneRetry = false) }
            return
        }

        mutableState.update { it.copy(capturing = true, sceneMessage = null) }
        viewModelScope.launch {
            scene.submit(challenge, challenge.clueId).fold(
                onSuccess = { mutableState.update { it.copy(capturing = false, submission = CameraSubmission.Awarded) } },
                onFailure = { error ->
                    if (error is CancellationException) throw error
                    val message = error.uiMessage(strings, strings.get(R.string.clue_confirmation_error))
                    mutableState.update { it.copy(capturing = false, sceneMessage = message, sceneRetry = true) }
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
            mutableState.update { it.copy(captureMessage = strings.get(R.string.clue_outside_frame)) }
            return
        }
        mutableState.update { it.copy(capturing = true, captureMessage = null, submission = null) }
        viewModelScope.launch {
            try {
                val path = captureClue(target, direction, camera)
                if (path == null) {
                    mutableState.update { it.copy(captureMessage = strings.get(R.string.clue_outside_frame)) }
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
                mutableState.update { it.copy(captureMessage = strings.get(R.string.photo_save_error)) }
            } finally {
                mutableState.update { it.copy(capturing = false) }
            }
        }
    }

    fun retrySync() {
        viewModelScope.launch {
            val result = progress.syncProgress()
            mutableState.update { it.copy(syncError = result.exceptionOrNull()?.uiMessage(strings, strings.get(R.string.sync_start_error))) }
        }
    }

    private fun observe(challenge: CameraChallenge) {
        observation?.cancel()
        observation = viewModelScope.launch {
            combine(repository.observeSubmission(challenge), repository.observeSubmissionError(challenge)) { submission, error ->
                submission to error
            }.collect { (submission, error) ->
                val message = error?.let { strings.domainMessage(it, strings.get(R.string.background_sync_error)) }
                mutableState.update { it.copy(submission = submission, syncError = message) }
            }
        }
    }
}

private fun Throwable.challengeLoadMessage(strings: UiStrings): String = when (this) {
    is DomainError.Validation -> strings.domainMessage(message, strings.get(R.string.puzzle_not_ready))
    else -> uiMessage(strings, strings.get(R.string.puzzles_load_error))
}
