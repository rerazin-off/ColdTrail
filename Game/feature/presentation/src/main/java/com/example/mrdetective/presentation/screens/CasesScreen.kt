package com.example.mrdetective.presentation.ui

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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

@Composable
internal fun CasesRoute(viewModel: CasesViewModel, onOpen: (Int) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CasesScreen(state, viewModel::reload, onOpen)
}

@Composable
private fun CasesScreen(state: CasesUiState, onRetry: () -> Unit, onOpen: (Int) -> Unit) {
    val colors = AppTheme.colors
    var query by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()
    Column(
        Modifier.fillMaxSize().padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(28.dp))
        Text("Ваши дела", style = MaterialTheme.typography.displayLarge)
        Text(
            "Незакрытые расследования требуют внимания.",
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
            label = { Text("Поиск по делам") },
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
        Text("Активные дела", color = colors.text2, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(8.dp))
        when (state) {
            CasesUiState.Loading -> LazyColumn(
                modifier = Modifier.semantics { contentDescription = "Загружаем дела" },
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                items(3) {
                    AppCard(outlined = true) {
                        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            listOf(0.68f, 0.88f, 0.50f, 0.32f).forEach { width ->
                                Box(Modifier.fillMaxWidth(width).height(8.dp).background(colors.surface2, AppShapes.control))
                            }
                        }
                    }
                }
            }
            CasesUiState.Empty -> AppCard(outlined = true) {
                Column(
                    Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Icon(painterResource(R.drawable.ic_search), null, Modifier.size(28.dp), tint = colors.accent)
                    Text("Пока нет дел", style = MaterialTheme.typography.headlineLarge)
                    Text("Новые расследования появятся здесь.", color = colors.text2, textAlign = TextAlign.Center)
                    AppButton("Обновить список", onRetry, modifier = Modifier.fillMaxWidth(0.75f))
                }
            }
            is CasesUiState.Error -> CasesMessage(
                if (state.unauthorized) "Откройте профиль и войдите в аккаунт повторно."
                else "Не удалось загрузить дела.",
                onRetry,
            )
            is CasesUiState.Content -> {
                val term = query.trim()
                val visibleCases = remember(state.cases, term) {
                    state.cases.filter {
                        it.title.contains(term, ignoreCase = true) ||
                            it.introduction.contains(term, ignoreCase = true)
                    }
                }
                LaunchedEffect(term) {
                    if (term.isNotEmpty()) listState.scrollToItem(0)
                }
                if (visibleCases.isEmpty()) {
                    CasesMessage("По вашему запросу ничего не найдено")
                } else {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(bottom = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        items(visibleCases, key = { it.id }) { case -> CaseListCard(case) { onOpen(case.id) } }
                    }
                }
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
                AppButton("Повторить", onRetry)
            }
        }
    }
}

@Composable
private fun CaseListCard(case: Case, onClick: () -> Unit) {
    val colors = AppTheme.colors
    val status = when (case.status) {
        "available" -> "Новое"
        "in_progress", "in_investigation" -> "В работе"
        "solved", "closed" -> "Закрыто"
        "failed" -> "Не раскрыто"
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
                Text("Расследование завершено", color = colors.text2, style = MaterialTheme.typography.bodySmall)
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
@Composable
private fun CasesPreview(@PreviewParameter(CasesPreviewStates::class) state: CasesUiState) {
    ColdTrailTheme(darkTheme = true) {
        Surface(color = AppTheme.colors.bg, contentColor = AppTheme.colors.text) {
            CasesScreen(state, onRetry = {}, onOpen = {})
        }
    }
}
