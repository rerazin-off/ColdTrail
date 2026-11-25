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

/** Represents the four display states of the case list. */
sealed interface CasesUiState {
    /** Indicates that the case catalog is being loaded. */
    data object Loading : CasesUiState

    /** Indicates that the catalog contains no cases for the player. */
    data object Empty : CasesUiState

    /** Holds the case catalog shown in the list. */
    data class Content(val cases: List<Case>) : CasesUiState

    /** Holds the error presented when the catalog could not be loaded. */
    data class Error(val unauthorized: Boolean, val message: String? = null) : CasesUiState
}

/** Loads and refreshes the case list displayed for the active session. */
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

        /** Clears the currently displayed case list state. */
        fun reset() {
            loadJob?.cancel()
            loadJob = null
            mutableState.value = CasesUiState.Loading
        }

        /** Reloads the case list for the active player. */
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
