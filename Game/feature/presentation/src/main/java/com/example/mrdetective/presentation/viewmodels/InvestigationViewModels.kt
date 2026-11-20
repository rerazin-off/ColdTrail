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
    val starting: Boolean = false,
    val started: Boolean = false,
    val status: String? = null,
    val collectedClueCount: Int = 0,
    val error: String? = null,
)

@HiltViewModel
internal class InvestigationViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val cases: CaseRepository,
    private val loadSuspects: LoadSuspectsUseCase,
    private val startInvestigation: StartInvestigationUseCase,
    private val restoreProgress: RestoreInvestigationProgressUseCase,
) : ViewModel() {
    private val caseId = savedState.toRoute<InvestigationRoute>().caseId
    private val mutableState = MutableStateFlow(InvestigationState())
    val state = mutableState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            val previous = state.value
            mutableState.value = previous.copy(loading = true, error = null)
            val case = cases.getCase(caseId).valueOrThrowCancellation()
            if (case == null) {
                mutableState.value = previous.copy(loading = false, error = "Не удалось загрузить дело.")
                return@launch
            }
            val suspects = loadSuspects(caseId).valueOrThrowCancellation()
            val progress = restoreProgress(caseId).valueOrThrowCancellation()
            mutableState.value = previous.copy(
                loading = false,
                case = case,
                suspects = suspects ?: previous.suspects,
                suspectsUnavailable = suspects == null,
                started = previous.started || progress != null,
                status = progress?.status ?: previous.status,
                collectedClueCount = progress?.collectedClueIds?.size ?: previous.collectedClueCount,
                error = null,
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
    val sending: Boolean = false,
    val historyUnavailable: Boolean = false,
    val feedback: String? = null,
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
            val messages = if (suspect == null) null else
                history(route.caseId, route.suspectId).valueOrThrowCancellation()
            mutableState.value = previous.copy(
                loading = false,
                suspect = suspect,
                messages = messages ?: previous.messages,
                historyUnavailable = messages == null,
                feedback = null,
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
            mutableState.value = current.copy(feedback = "Сократите сообщение, чтобы прикрепить улику.")
            return
        }
        mutableState.value = current.copy(sending = true, feedback = null)
        viewModelScope.launch {
            val result = if (clue == null) sendMessage(route.caseId, route.suspectId, text)
            else presentClue(route.caseId, route.suspectId, clue.id, text)
            val message = result.valueOrThrowCancellation()
            if (message == null) {
                mutableState.value = current.copy(feedback = "Не удалось отправить сообщение.")
                return@launch
            }
            savedState["draft"] = ""
            mutableAttachment.value = null
            val updated = history(route.caseId, route.suspectId).valueOrThrowCancellation()
            mutableState.value = current.copy(
                messages = if (updated.isNullOrEmpty()) (current.messages + message).distinctBy { it.id } else updated,
                historyUnavailable = updated == null,
                feedback = if (message.author == MessageAuthor.PLAYER) "Сообщение сохранено. Ответ пока недоступен." else null,
            )
        }
    }
}

private fun <T> Result<T>.valueOrThrowCancellation(): T? {
    val failure = exceptionOrNull()
    if (failure is CancellationException) throw failure
    return getOrNull()
}
