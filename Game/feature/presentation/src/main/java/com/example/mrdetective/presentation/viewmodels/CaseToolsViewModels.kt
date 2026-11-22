package com.example.mrdetective.presentation.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.mrdetective.domain.models.Accusation
import com.example.mrdetective.domain.models.Clue
import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.models.Suspect
import com.example.mrdetective.domain.repositories.AccusationRepository
import com.example.mrdetective.domain.usecases.accusation.SubmitAccusationUseCase
import com.example.mrdetective.domain.usecases.cases.LoadSuspectsUseCase
import com.example.mrdetective.domain.usecases.cases.RestoreInvestigationProgressUseCase
import com.example.mrdetective.domain.usecases.inventory.LoadInventoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
internal data class InventoryRoute(val caseId: Int)

@Serializable
internal data class MorseRoute(val caseId: Int, val puzzleId: Int? = null)

@Serializable
internal data class AccusationRoute(val caseId: Int)

@HiltViewModel
internal class InventoryViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val restoreProgress: RestoreInvestigationProgressUseCase,
    private val loadInventory: LoadInventoryUseCase,
) : ViewModel() {
    private val caseId = savedState.toRoute<InventoryRoute>().caseId
    private val mutableState = MutableStateFlow<CluePickerState>(CluePickerState.Loading)
    val state = mutableState.asStateFlow()
    private var loadJob: Job? = null

    init {
        load()
    }

    fun load() {
        if (loadJob?.isActive == true) return
        loadJob = viewModelScope.launch {
            mutableState.value = CluePickerState.Loading
            mutableState.value = try {
                val progress = restoreProgress(caseId).getOrThrow()
                CluePickerState.Content(loadInventory(progress.id).getOrThrow())
            } catch (error: CancellationException) {
                throw error
            } catch (_: DomainError.NotFound) {
                CluePickerState.Content(emptyList())
            } catch (_: Exception) {
                CluePickerState.Error
            }
        }
    }
}

internal data class AccusationState(
    val loading: Boolean = true,
    val suspects: List<Suspect> = emptyList(),
    val submitting: Boolean = false,
    val result: Accusation? = null,
    val error: String? = null,
    val closed: Boolean = false,
)

@HiltViewModel
internal class AccusationViewModel @Inject constructor(
    private val savedState: SavedStateHandle,
    private val loadSuspects: LoadSuspectsUseCase,
    private val restoreProgress: RestoreInvestigationProgressUseCase,
    private val submitAccusation: SubmitAccusationUseCase,
    private val accusations: AccusationRepository,
) : ViewModel() {
    private val caseId = savedState.toRoute<AccusationRoute>().caseId
    private val mutableState = MutableStateFlow(AccusationState())
    val state = mutableState.asStateFlow()
    val selectedId = savedState.getStateFlow<Int?>("accusedSuspect", null)
    private var loadJob: Job? = null

    init {
        load()
    }

    fun load() {
        if (loadJob?.isActive == true || state.value.submitting) return
        loadJob = viewModelScope.launch {
            mutableState.value = AccusationState()
            var suspects = emptyList<Suspect>()
            var closed = false
            mutableState.value = try {
                suspects = loadSuspects(caseId).getOrThrow()
                val progress = restoreProgress(caseId).getOrThrow()
                closed = progress.status in setOf("solved", "failed", "closed")
                val result = if (closed) accusations.getResult(caseId).getOrThrow() else null
                AccusationState(loading = false, suspects = suspects, result = result, closed = closed)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                AccusationState(
                    loading = false,
                    suspects = suspects,
                    closed = closed,
                    error = if (closed) "Не удалось загрузить итог расследования."
                    else if (error is DomainError.NotFound) "Сначала начните расследование."
                    else "Не удалось открыть обвинение.",
                )
            }
        }
    }

    fun select(suspectId: Int) {
        if (!state.value.submitting) savedState["accusedSuspect"] = suspectId
    }

    fun accuse() {
        val current = state.value
        val suspect = current.suspects.firstOrNull { it.id == selectedId.value } ?: return
        if (current.loading || current.submitting || current.closed || current.result != null) return
        mutableState.value = current.copy(submitting = true, error = null)
        viewModelScope.launch {
            mutableState.value = try {
                val result = submitAccusation(caseId, suspect.id, emptyList()).getOrThrow()
                current.copy(result = result, closed = true)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                current.copy(
                    error = if (error is DomainError.Network) {
                        "Для обвинения нужно подключение к сети и синхронизированное расследование."
                    } else {
                        "Не удалось отправить обвинение. Попробуйте ещё раз."
                    },
                )
            }
        }
    }
}
