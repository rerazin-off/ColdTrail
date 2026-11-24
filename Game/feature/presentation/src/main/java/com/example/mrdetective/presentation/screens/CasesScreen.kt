package com.example.mrdetective.presentation.ui

import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mrdetective.domain.models.Case
import com.example.mrdetective.presentation.R
import com.example.mrdetective.presentation.ui.components.AppButton
import com.example.mrdetective.presentation.ui.components.AppCard
import com.example.mrdetective.presentation.ui.theme.AppShapes
import com.example.mrdetective.presentation.ui.theme.AppTheme
import com.example.mrdetective.presentation.ui.theme.ColdTrailTheme
import com.example.mrdetective.presentation.ui.theme.LocalAppWindow
import com.example.mrdetective.presentation.ui.theme.AppWindow
import com.example.mrdetective.presentation.ui.theme.isMediumWidth

@Composable
internal fun CasesRoute(viewModel: CasesViewModel, onOpen: (Int) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CasesScreen(state, viewModel::reload, onOpen)
}

@Composable
private fun CasesScreen(state: CasesUiState, onRetry: () -> Unit, onOpen: (Int) -> Unit) {
    val colors = AppTheme.colors
    var query by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyGridState()
    val term = query.trim()
    val cases = (state as? CasesUiState.Content)?.cases.orEmpty()
    val visibleCases = remember(cases, term) {
        cases.filter { it.title.contains(term, ignoreCase = true) || it.introduction.contains(term, ignoreCase = true) }
    }
    LaunchedEffect(term) {
        if (term.isNotEmpty()) listState.scrollToItem(0)
    }
    val loadingDescription = stringResource(R.string.loading_cases)
    LazyVerticalGrid(
        columns = if (LocalAppWindow.current.isMediumWidth) GridCells.Adaptive(280.dp) else GridCells.Fixed(1),
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        state = listState,
        contentPadding = PaddingValues(bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (state is CasesUiState.Error) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                ScreenRetry(state.message ?: stringResource(R.string.cases_load_error), onRetry)
            }
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column {
                Spacer(Modifier.height(28.dp))
                Text(stringResource(R.string.your_cases), style = MaterialTheme.typography.displayLarge)
                Text(
                    stringResource(R.string.cases_intro),
                    color = colors.text2,
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(Modifier.height(20.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state is CasesUiState.Content,
                    singleLine = true,
                    shape = AppShapes.control,
                    label = { Text(stringResource(R.string.case_search)) },
                    leadingIcon = { Icon(painterResource(R.drawable.ic_search), null, Modifier.size(18.dp)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.surface,
                        unfocusedContainerColor = colors.surface,
                        disabledContainerColor = colors.surface,
                        focusedBorderColor = colors.onAccent,
                        unfocusedBorderColor = colors.outline,
                    ),
                )
                Spacer(Modifier.height(16.dp))
                Text(stringResource(R.string.active_cases), color = colors.text2, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
            }
        }
        when (state) {
            CasesUiState.Loading -> items(3) { index ->
                val semantics = if (index == 0) Modifier.semantics { contentDescription = loadingDescription } else Modifier
                AppCard(semantics, outlined = true) {
                    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        listOf(0.68f, 0.88f, 0.50f, 0.32f).forEach { width ->
                            Box(Modifier.fillMaxWidth(width).height(8.dp).background(colors.surface2, AppShapes.control))
                        }
                    }
                }
            }
            CasesUiState.Empty -> item(span = { GridItemSpan(maxLineSpan) }) {
                AppCard(outlined = true) {
                    Column(
                        Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Icon(painterResource(R.drawable.ic_search), null, Modifier.size(28.dp), tint = colors.accent)
                        Text(stringResource(R.string.no_cases), style = MaterialTheme.typography.headlineLarge)
                        Text(stringResource(R.string.new_cases_info), color = colors.text2, textAlign = TextAlign.Center)
                        AppButton(stringResource(R.string.refresh_cases), onRetry, modifier = Modifier.fillMaxWidth(0.75f))
                    }
                }
            }
            is CasesUiState.Error -> Unit
            is CasesUiState.Content -> if (visibleCases.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) { CasesMessage(stringResource(R.string.no_search_results)) }
            } else {
                items(visibleCases, key = { it.id }) { case -> CaseListCard(case) { onOpen(case.id) } }
            }
        }
    }
}

@Composable
private fun CasesMessage(message: String, onRetry: (() -> Unit)? = null) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(message, textAlign = TextAlign.Center)
            if (onRetry != null) {
                Spacer(Modifier.height(12.dp))
                AppButton(stringResource(R.string.retry), onRetry)
            }
        }
    }
}

@Composable
private fun CaseListCard(case: Case, onClick: () -> Unit) {
    val colors = AppTheme.colors
    val status = when (case.status) {
        "available" -> stringResource(R.string.case_new)
        "in_progress", "in_investigation" -> stringResource(R.string.case_in_progress)
        "solved", "closed" -> stringResource(R.string.case_closed)
        "failed" -> stringResource(R.string.case_unsolved)
        else -> null
    }
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.card,
        color = colors.surface,
        border = BorderStroke(1.dp, colors.outline),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(case.title, style = MaterialTheme.typography.headlineMedium)
            Text(case.introduction, color = colors.text2, style = MaterialTheme.typography.bodySmall)
            if (status != null) {
                Text(
                    status,
                    color = if (case.status == "solved" || case.status == "closed") {
                        colors.success
                    } else {
                        colors.onAccent
                    },
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            if (case.status in setOf("solved", "closed", "failed")) {
                Text(stringResource(R.string.case_finished), color = colors.text2, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

class CasesPreviewStates : PreviewParameterProvider<CasesUiState> {
    override val values = sequenceOf(
        CasesUiState.Loading,
        CasesUiState.Content(
            listOf(
                Case(1, "Дело №01: Пропажа", "Из галереи исчезла картина", status = "in_progress"),
                Case(2, "Дело №02: Чужой звонок", "Неизвестный номер"),
                Case(3, "Дело №03: Последний поезд", "Исчезновение на вокзале", status = "solved"),
            ),
        ),
        CasesUiState.Empty,
        CasesUiState.Error(unauthorized = false),
    )
}

@Preview(widthDp = 390, heightDp = 760)
@Preview(name = "Landscape", widthDp = 780, heightDp = 390)
@Preview(name = "Tablet, large text", widthDp = 1000, heightDp = 750, fontScale = 2f, locale = "en")
@Composable
private fun CasesPreview(@PreviewParameter(CasesPreviewStates::class) state: CasesUiState) {
    ColdTrailTheme(darkTheme = true) {
        Surface(color = AppTheme.colors.bg, contentColor = AppTheme.colors.text) {
            AppWindow { CasesScreen(state, onRetry = {}, onOpen = {}) }
        }
    }
}
