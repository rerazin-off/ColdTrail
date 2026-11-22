package com.example.mrdetective.presentation.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.mrdetective.domain.models.Clue
import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.models.MorseChallenge
import com.example.mrdetective.domain.repositories.MorseChallengeRepository
import com.example.mrdetective.domain.usecases.inventory.LoadInventoryUseCase
import com.example.mrdetective.domain.usecases.puzzles.PlayPuzzleUseCase
import com.example.mrdetective.domain.usecases.puzzles.chooseMorseChallenge
import com.example.mrdetective.domain.usecases.puzzles.validateMorseChallengeAnswer
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal data class MorseGameState(
    val loading: Boolean = true,
    val challenge: MorseChallenge? = null,
    val investigationId: Int = 0,
    val submitting: Boolean = false,
    val solved: Boolean = false,
    val reward: Clue? = null,
    val loadError: String? = null,
    val answerError: String? = null,
)

@HiltViewModel
internal class MorseChallengeViewModel @Inject constructor(
    private val savedState: SavedStateHandle,
    private val challenges: MorseChallengeRepository,
    private val playPuzzle: PlayPuzzleUseCase,
    private val loadInventory: LoadInventoryUseCase,
) : ViewModel() {
    private val route = savedState.toRoute<MorseRoute>()
    private val mutableState = MutableStateFlow(MorseGameState())
    val state = mutableState.asStateFlow()
    val answer = savedState.getStateFlow("morseAnswer", "")
    private var loadJob: Job? = null

    init {
        load()
    }

    fun load() {
        if (loadJob?.isActive == true || state.value.submitting) return
        mutableState.value = MorseGameState()
        loadJob = viewModelScope.launch {
            mutableState.value = try {
                val available = challenges.load(route.caseId).getOrThrow()
                val restoredId = savedState.get<Int>("morsePuzzle")
                val candidates = route.puzzleId?.let { id -> available.puzzles.filter { it.id == id } } ?: available.puzzles
                val challenge = chooseMorseChallenge(candidates, restoredId ?: route.puzzleId)
                if (challenge?.id != restoredId) savedState["morseAnswer"] = ""
                savedState["morsePuzzle"] = challenge?.id
                MorseGameState(
                    loading = false,
                    challenge = challenge,
                    investigationId = available.investigationId,
                    solved = challenge?.solved == true,
                    reward = if (challenge?.solved == true) findReward(available.investigationId, challenge.rewardClueId) else null,
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                MorseGameState(
                    loading = false,
                    loadError = when (error) {
                        is DomainError.Validation -> error.message
                        is DomainError.Network -> "Для выбора загадки нужно подключение к сети."
                        else -> "Не удалось загрузить загадку. Попробуйте ещё раз."
                    },
                )
            }
        }
    }

    fun updateAnswer(value: String) {
        val current = state.value
        val challenge = current.challenge ?: return
        if (current.submitting || current.solved) return
        savedState["morseAnswer"] = value.take(challenge.answerLength)
        mutableState.update { it.copy(answerError = null) }
    }

    fun submit() {
        val current = state.value
        val challenge = current.challenge ?: return
        if (current.solved || current.submitting) return
        val submittedAnswer = answer.value.trim()
        if (!validateMorseChallengeAnswer(challenge, submittedAnswer)) {
            mutableState.update { it.copy(answerError = "Введите ${challenge.answerLength} знаков ответа.") }
            return
        }
        mutableState.update { it.copy(submitting = true, answerError = null) }
        viewModelScope.launch {
            try {
                val correct = playPuzzle(current.investigationId, challenge.id, submittedAnswer).getOrThrow()
                val reward = if (correct) findReward(current.investigationId, challenge.rewardClueId) else null
                mutableState.update {
                    it.copy(
                        submitting = false,
                        solved = correct,
                        reward = reward,
                        answerError = if (correct) null else "Неверный ответ. Проверьте сигнал.",
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                val message = if (error is DomainError.Network) {
                    "Ответ пока не подтверждён сервером. Проверьте подключение."
                } else {
                    "Не удалось проверить ответ. Попробуйте ещё раз."
                }
                mutableState.update { it.copy(submitting = false, answerError = message) }
            }
        }
    }

    private suspend fun findReward(investigationId: Int, clueId: Int): Clue? =
        loadInventory(investigationId).getOrElse {
            if (it is CancellationException) throw it
            emptyList()
        }.firstOrNull { it.id == clueId }
}
