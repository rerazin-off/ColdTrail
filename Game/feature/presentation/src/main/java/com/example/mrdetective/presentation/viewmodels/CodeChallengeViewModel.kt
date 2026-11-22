package com.example.mrdetective.presentation.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.mrdetective.domain.models.Clue
import com.example.mrdetective.domain.models.CodeChallenge
import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.repositories.CodeChallengeRepository
import com.example.mrdetective.domain.usecases.inventory.LoadInventoryUseCase
import com.example.mrdetective.domain.usecases.puzzles.CODE_CHALLENGE_LENGTH
import com.example.mrdetective.domain.usecases.puzzles.PlayCodeChallengeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Serializable
internal data class CodeChallengeRoute(val caseId: Int, val puzzleId: Int)

internal data class CodeChallengeState(
    val loading: Boolean = true,
    val investigationId: Int = 0,
    val challenge: CodeChallenge? = null,
    val submitting: Boolean = false,
    val solved: Boolean = false,
    val reward: Clue? = null,
    val loadError: String? = null,
    val answerError: String? = null,
)

@HiltViewModel
internal class CodeChallengeViewModel @Inject constructor(
    private val savedState: SavedStateHandle,
    private val challenges: CodeChallengeRepository,
    private val playCode: PlayCodeChallengeUseCase,
    private val loadInventory: LoadInventoryUseCase,
) : ViewModel() {
    private val route = savedState.toRoute<CodeChallengeRoute>()
    private val mutableState = MutableStateFlow(CodeChallengeState())
    val state = mutableState.asStateFlow()
    val answer = savedState.getStateFlow("codeAnswer", "")
    private var loadJob: Job? = null

    init {
        load()
    }

    fun load() {
        if (loadJob?.isActive == true || state.value.submitting) return
        mutableState.value = CodeChallengeState()
        loadJob = viewModelScope.launch {
            challenges.load(route.caseId).fold(
                { available ->
                    val puzzle = available.puzzles.firstOrNull { it.id == route.puzzleId && (it.solved || !it.rewardClaimed) }
                    val solved = puzzle?.solved == true
                    mutableState.value = CodeChallengeState(
                        loading = false,
                        investigationId = available.investigationId,
                        challenge = puzzle,
                        solved = solved,
                        reward = if (puzzle != null && solved) findReward(available.investigationId, puzzle.rewardClueId) else null,
                    )
                },
                { error ->
                    if (error is CancellationException) throw error
                    mutableState.value = CodeChallengeState(
                        loading = false,
                        loadError = (error as? DomainError.Validation)?.message ?: "Не удалось загрузить загадку.",
                    )
                },
            )
        }
    }

    fun updateAnswer(value: String) {
        val current = state.value
        if (current.challenge == null || current.submitting || current.solved) return
        savedState["codeAnswer"] = value.filter { it in '0'..'9' }.take(CODE_CHALLENGE_LENGTH)
        mutableState.update { it.copy(answerError = null) }
    }

    fun submit() {
        val current = state.value
        val puzzle = current.challenge ?: return
        if (current.submitting || current.solved) return
        val submitted = answer.value
        mutableState.update { it.copy(submitting = true, answerError = null) }
        viewModelScope.launch {
            playCode(current.investigationId, puzzle.id, submitted).fold(
                { correct ->
                    val reward = if (correct) findReward(current.investigationId, puzzle.rewardClueId) else null
                    mutableState.update {
                        it.copy(
                            submitting = false,
                            solved = correct,
                            reward = reward,
                            answerError = if (correct) null else "Неверный код. Проверьте подсказку.",
                        )
                    }
                },
                { error ->
                    if (error is CancellationException) throw error
                    val message = when (error) {
                        is DomainError.Validation -> error.message ?: "Введите четыре цифры."
                        is DomainError.Network -> "Ответ пока не подтверждён сервером. Проверьте подключение."
                        else -> "Не удалось проверить код. Попробуйте ещё раз."
                    }
                    mutableState.update { it.copy(submitting = false, answerError = message) }
                },
            )
        }
    }

    private suspend fun findReward(investigationId: Int, clueId: Int): Clue? =
        loadInventory(investigationId).getOrElse {
            if (it is CancellationException) throw it
            emptyList()
        }.firstOrNull { it.id == clueId }
}
