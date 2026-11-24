package com.example.mrdetective.presentation.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.mrdetective.domain.models.Case
import com.example.mrdetective.domain.models.Clue
import com.example.mrdetective.domain.models.DialogueMessage
import com.example.mrdetective.domain.models.MessageAuthor
import com.example.mrdetective.domain.models.Suspect
import com.example.mrdetective.domain.repositories.CaseRepository
import com.example.mrdetective.domain.repositories.ProgressRepository
import com.example.mrdetective.domain.usecases.cases.LoadSuspectsUseCase
import com.example.mrdetective.domain.usecases.cases.RestoreInvestigationProgressUseCase
import com.example.mrdetective.domain.usecases.cases.StartInvestigationUseCase
import com.example.mrdetective.domain.usecases.dialogue.LoadDialogueHistoryUseCase
import com.example.mrdetective.domain.usecases.dialogue.PresentClueToSuspectUseCase
import com.example.mrdetective.domain.usecases.dialogue.SendSuspectMessageUseCase
import com.example.mrdetective.domain.usecases.inventory.LoadInventoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
internal data class InvestigationRoute(val caseId: Int)

@Serializable
internal data class InterrogationRoute(val caseId: Int, val suspectId: Int)

internal data class InvestigationState(
    val loading: Boolean = true,
    val case: Case? = null,
    val suspects: List<Suspect> = emptyList(),
    val suspectsUnavailable: Boolean = false,
    val suspectsError: String? = null,
    val starting: Boolean = false,
    val started: Boolean = false,
    val status: String? = null,
    val collectedClueIds: Set<Int> = emptySet(),
    val error: String? = null,
) {
    val totalClueCount get() = case?.clueIds?.size
    val collectedClueCount get() = case?.clueIds?.count { it in collectedClueIds } ?: collectedClueIds.size
    val hasRemainingClues get() = totalClueCount?.let { collectedClueCount < it } ?: true
}

@HiltViewModel
internal class InvestigationViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val cases: CaseRepository,
    private val loadSuspects: LoadSuspectsUseCase,
    private val startInvestigation: StartInvestigationUseCase,
    private val restoreProgress: RestoreInvestigationProgressUseCase,
    private val progressRepository: ProgressRepository,
) : ViewModel() {
    private val caseId = savedState.toRoute<InvestigationRoute>().caseId
    private val mutableState = MutableStateFlow(InvestigationState())
    val state = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            progressRepository.observeCollectedClueIds(caseId).collect { clueIds ->
                mutableState.update { it.copy(collectedClueIds = clueIds) }
            }
        }
        load()
    }

    fun load() {
        viewModelScope.launch {
            val previous = state.value
            mutableState.value = previous.copy(loading = true, error = null)
            val caseResult = cases.getCase(caseId)
            val case = caseResult.valueOrThrowCancellation()
            if (case == null) {
                val message = caseResult.exceptionOrNull()?.uiMessage("Не удалось загрузить дело.") ?: "Дело недоступно."
                mutableState.update { it.copy(loading = false, error = message) }
                return@launch
            }
            val suspectsResult = loadSuspects(caseId)
            val suspects = suspectsResult.valueOrThrowCancellation()
            val progress = restoreProgress(caseId).valueOrThrowCancellation()
            mutableState.update {
                it.copy(
                    loading = false,
                    case = case,
                    suspects = suspects ?: it.suspects,
                    suspectsUnavailable = suspects == null,
                    suspectsError = suspectsResult.exceptionOrNull()?.uiMessage("Не удалось загрузить подозреваемых."),
                    started = it.started || progress != null,
                    status = progress?.status ?: it.status,
                    error = null,
                )
            }
        }
    }

    fun refreshProgress() {
        if (state.value.loading || state.value.starting) return
        viewModelScope.launch {
            val progress = restoreProgress(caseId).valueOrThrowCancellation() ?: return@launch
            mutableState.value = state.value.copy(
                started = true,
                status = progress.status,
            )
        }
    }

    fun start() {
        val current = state.value
        if (current.case == null || current.loading || current.starting || current.started) return
        mutableState.value = current.copy(starting = true, error = null)
        viewModelScope.launch {
            val started = startInvestigation(caseId).valueOrThrowCancellation() != null
            val status = if (started) restoreProgress(caseId).valueOrThrowCancellation()?.status ?: "in_progress" else null
            mutableState.value = state.value.copy(
                starting = false,
                started = started,
                status = status,
                error = if (started) null else "Не удалось начать расследование.",
            )
        }
    }
}

internal data class InterrogationState(
    val loading: Boolean = true,
    val suspect: Suspect? = null,
    val messages: List<DialogueMessage> = emptyList(),
    val animatedMessageIds: Set<Int> = emptySet(),
    val sending: Boolean = false,
    val historyUnavailable: Boolean = false,
    val historyError: String? = null,
    val feedback: String? = null,
    val pendingFeedback: Boolean = false,
)

internal sealed interface CluePickerState {
    data object Loading : CluePickerState
    data object Error : CluePickerState
    data class Content(val clues: List<Clue>) : CluePickerState
}

@HiltViewModel
internal class InterrogationViewModel @Inject constructor(
    private val savedState: SavedStateHandle,
    private val loadSuspects: LoadSuspectsUseCase,
    private val history: LoadDialogueHistoryUseCase,
    private val sendMessage: SendSuspectMessageUseCase,
    private val presentClue: PresentClueToSuspectUseCase,
    private val restoreProgress: RestoreInvestigationProgressUseCase,
    private val loadInventory: LoadInventoryUseCase,
    private val progressRepository: ProgressRepository,
) : ViewModel() {
    private val route = savedState.toRoute<InterrogationRoute>()
    val draft = savedState.getStateFlow("draft", "")
    private val mutableState = MutableStateFlow(InterrogationState())
    val state = mutableState.asStateFlow()
    private val mutableClues = MutableStateFlow<CluePickerState>(CluePickerState.Loading)
    val clues = mutableClues.asStateFlow()
    private val mutableAttachment = MutableStateFlow<Clue?>(null)
    val attachment = mutableAttachment.asStateFlow()
    private var inventoryJob: kotlinx.coroutines.Job? = null

    init {
        load()
    }

    fun load() {
        if (state.value.sending) return
        viewModelScope.launch {
            val previous = state.value
            mutableState.value = previous.copy(loading = true, feedback = null)
            val suspect = previous.suspect ?: loadSuspects(route.caseId).valueOrThrowCancellation()
                ?.firstOrNull { it.id == route.suspectId }
            val historyResult = if (suspect == null) null else history(route.caseId, route.suspectId)
            val messages = historyResult?.valueOrThrowCancellation()
            mutableState.value = previous.copy(
                loading = false,
                suspect = suspect,
                messages = messages ?: previous.messages,
                historyUnavailable = messages == null,
                historyError = historyResult?.exceptionOrNull()?.uiMessage("Не удалось обновить историю."),
                feedback = null,
                pendingFeedback = false,
            )
        }
    }

    fun updateDraft(text: String) {
        savedState["draft"] = text.take(1000)
    }

    fun loadClues() {
        if (inventoryJob?.isActive == true) return
        mutableClues.value = CluePickerState.Loading
        inventoryJob = viewModelScope.launch {
            val progress = restoreProgress(route.caseId).valueOrThrowCancellation()
            val items = progress?.let { loadInventory(it.id).valueOrThrowCancellation() }
            mutableClues.value = if (items == null) CluePickerState.Error
            else CluePickerState.Content(items.filter { it.caseId == route.caseId })
        }
    }

    fun attach(clue: Clue?) {
        if (!state.value.sending) mutableAttachment.value = clue
    }

    fun send() {
        val question = draft.value.trim()
        val clue = attachment.value
        val current = state.value
        if (current.suspect == null || current.loading || current.sending || (question.isBlank() && clue == null)) return
        val text = if (clue == null) question else {
            val label = "Улика: ${clue.title.take(200)}"
            if (question.isBlank()) label else "$question\n\n$label"
        }
        if (text.length > 1000) {
            mutableState.value = current.copy(feedback = "Сократите сообщение, чтобы прикрепить улику.", pendingFeedback = false)
            return
        }
        mutableState.value = current.copy(sending = true, feedback = null)
        viewModelScope.launch {
            val result = if (clue == null) sendMessage(route.caseId, route.suspectId, text)
            else presentClue(route.caseId, route.suspectId, clue.id, text)
            val message = result.valueOrThrowCancellation()
            if (message == null) {
                val failure = result.exceptionOrNull()
                mutableState.value = current.copy(
                    feedback = failure?.uiMessage("Не удалось отправить сообщение.") ?: "Не удалось отправить сообщение.",
                    pendingFeedback = false,
                )
                return@launch
            }
            savedState["draft"] = ""
            mutableAttachment.value = null
            val updated = history(route.caseId, route.suspectId).valueOrThrowCancellation()
            val messages = if (updated.isNullOrEmpty()) (current.messages + message).distinctBy { it.id } else updated
            val oldIds = current.messages.mapTo(mutableSetOf()) { it.id }
            val outgoing = messages.lastOrNull { it.id !in oldIds && it.author == MessageAuthor.PLAYER && it.text == text }
            mutableState.value = current.copy(
                messages = messages,
                animatedMessageIds = setOfNotNull(message.id, outgoing?.id) - oldIds,
                historyUnavailable = updated == null,
                feedback = if (message.author == MessageAuthor.PLAYER) "Реплика сохранена на телефоне. Она отправится, когда сервер станет доступен." else null,
                pendingFeedback = message.author == MessageAuthor.PLAYER,
            )
        }
    }

    fun retryPendingMessage() {
        viewModelScope.launch {
            val result = progressRepository.syncProgress()
            mutableState.update { it.copy(feedback = result.exceptionOrNull()?.uiMessage("Не удалось запустить синхронизацию.")) }
        }
    }
}

private fun <T> Result<T>.valueOrThrowCancellation(): T? {
    val failure = exceptionOrNull()
    if (failure is CancellationException) throw failure
    return getOrNull()
}
