package com.example.mrdetective.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mrdetective.domain.usecases.puzzles.CODE_CHALLENGE_LENGTH
import com.example.mrdetective.presentation.ui.components.AppCard
import com.example.mrdetective.presentation.ui.components.ClueRewardDialog
import com.example.mrdetective.presentation.ui.components.ErrorNotice
import com.example.mrdetective.presentation.ui.components.NumericPuzzleKeypad
import com.example.mrdetective.presentation.ui.components.NumericPuzzleSlots
import com.example.mrdetective.presentation.ui.theme.AppTheme

@Composable
internal fun CodeChallengeScreen(viewModel: CodeChallengeViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val code by viewModel.answer.collectAsStateWithLifecycle()
    val colors = AppTheme.colors
    if (state.solved) {
        ClueRewardDialog(state.reward?.title, state.reward?.description, onBack)
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CaseToolHeader("Кодовый замок", onBack)
        val challenge = state.challenge
        when {
            state.loading -> CircularProgressIndicator(color = colors.accent)
            state.loadError != null -> ScreenRetry(state.loadError.orEmpty(), viewModel::load)
            challenge == null -> AppCard(outlined = true) { Text("Эта загадка больше недоступна.") }
            else -> {
                Text(challenge.title, style = MaterialTheme.typography.displayLarge)
                challenge.hint?.takeIf { it.isNotBlank() }?.let { hint ->
                    AppCard(outlined = true) { Text(hint, style = MaterialTheme.typography.bodyMedium) }
                }
                AppCard(outlined = true) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Введите четырёхзначный код", style = MaterialTheme.typography.bodySmall, color = colors.text2)
                        NumericPuzzleSlots(CODE_CHALLENGE_LENGTH, code, state.solved, state.answerError != null)
                        if (state.submitting) Text("Проверяем код…", color = colors.text2)
                        ErrorNotice(state.answerError, when {
                            state.retrySync -> viewModel::retrySync
                            state.retryAnswer -> viewModel::submit
                            else -> null
                        })
                    }
                }
                if (!state.solved) {
                    NumericPuzzleKeypad(
                        onDigit = { viewModel.updateAnswer(viewModel.answer.value + it) },
                        onDelete = { viewModel.updateAnswer(viewModel.answer.value.dropLast(1)) },
                        onSubmit = viewModel::submit,
                        enabled = !state.submitting,
                        canSubmit = !state.submitting && code.length == CODE_CHALLENGE_LENGTH,
                    )
                }
            }
        }
    }
}
