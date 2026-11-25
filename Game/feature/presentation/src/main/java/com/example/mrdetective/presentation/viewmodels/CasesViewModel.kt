package com.example.mrdetective.presentation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mrdetective.domain.models.Case
import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.usecases.cases.LoadAvailableCasesUseCase
import com.example.mrdetective.presentation.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

/** Описывает четыре состояния отображения списка дел. */
sealed interface CasesUiState {
    /** Обозначает загрузку списка дел. */
    data object Loading : CasesUiState

    /** Обозначает отсутствие доступных игроку дел. */
    data object Empty : CasesUiState

    /** Хранит дела для отображения в списке. */
    data class Content(val cases: List<Case>) : CasesUiState

    /** Хранит ошибку, возникшую при загрузке списка дел. */
    data class Error(val unauthorized: Boolean, val message: String? = null) : CasesUiState
}

/** Загружает и обновляет список дел для текущей сессии. */
@HiltViewModel
class CasesViewModel
    @Inject
    constructor(
        private val strings: UiStrings,
        private val loadAvailableCases: LoadAvailableCasesUseCase,
    ) : ViewModel() {
        private val mutableState = MutableStateFlow<CasesUiState>(CasesUiState.Loading)
        val state = mutableState.asStateFlow()

        private var loadJob: Job? = null

        internal fun updateCaseStatus(
            caseId: Int,
            status: String,
        ) {
            mutableState.update { current ->
                if (current is CasesUiState.Content) {
                    current.copy(cases = current.cases.map { if (it.id == caseId) it.copy(status = status) else it })
                } else {
                    current
                }
            }
        }

        /** Сбрасывает текущее состояние списка дел. */
        fun reset() {
            loadJob?.cancel()
            loadJob = null
            mutableState.value = CasesUiState.Loading
        }

        /** Перезагружает список дел текущего игрока. */
        fun reload() {
            if (loadJob?.isActive == true) return
            loadJob =
                viewModelScope.launch {
                    if (state.value !is CasesUiState.Content) mutableState.value = CasesUiState.Loading
                    mutableState.value =
                        try {
                            val cases = loadAvailableCases().getOrThrow()
                            if (cases.isEmpty()) CasesUiState.Empty else CasesUiState.Content(cases)
                        } catch (error: CancellationException) {
                            throw error
                        } catch (error: Exception) {
                            CasesUiState.Error(
                                error is DomainError.Unauthorized,
                                error.uiMessage(strings, strings.get(R.string.cases_load_error)),
                            )
                        }
                }
        }
    }
