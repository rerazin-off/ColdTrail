package com.example.mrdetective.presentation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mrdetective.domain.models.Case
import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.usecases.cases.LoadAvailableCasesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

sealed interface CasesUiState {
    data object Loading : CasesUiState
    data object Empty : CasesUiState
    data class Content(val cases: List<Case>) : CasesUiState
    data class Error(val unauthorized: Boolean) : CasesUiState
}

@HiltViewModel
class CasesViewModel @Inject constructor(
    private val loadAvailableCases: LoadAvailableCasesUseCase,
) : ViewModel() {
    private val mutableState = MutableStateFlow<CasesUiState>(CasesUiState.Loading)
    val state = mutableState.asStateFlow()

    private var loadJob: Job? = null

    init {
        reload()
    }

    fun reload() {
        if (loadJob?.isActive == true) return
        loadJob = viewModelScope.launch {
            mutableState.value = CasesUiState.Loading
            mutableState.value = try {
                val cases = loadAvailableCases().getOrThrow()
                if (cases.isEmpty()) CasesUiState.Empty else CasesUiState.Content(cases)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                CasesUiState.Error(unauthorized = error is DomainError.Unauthorized)
            }
        }
    }
}
