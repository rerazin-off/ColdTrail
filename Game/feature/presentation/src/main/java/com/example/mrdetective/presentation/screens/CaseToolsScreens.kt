package com.example.mrdetective.presentation.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mrdetective.domain.usecases.puzzles.DemoMorsePuzzle
import com.example.mrdetective.domain.usecases.puzzles.MorseCodeCheck
import com.example.mrdetective.presentation.R
import com.example.mrdetective.presentation.ui.components.AppButton
import com.example.mrdetective.presentation.ui.components.AppButtonStyle
import com.example.mrdetective.presentation.ui.components.AppCard
import com.example.mrdetective.presentation.ui.components.ClueCard
import com.example.mrdetective.presentation.ui.theme.AppShapes
import com.example.mrdetective.presentation.ui.theme.AppTheme

@Composable
private fun CaseToolHeader(title: String, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onBack) {
            Icon(painterResource(R.drawable.ic_chat_back), "Назад", tint = AppTheme.colors.text)
        }
        Text(title, style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
internal fun ProfileScreen(auth: AuthUiState, onBack: () -> Unit, onSignOut: () -> Unit) {
    val colors = AppTheme.colors
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.fillMaxWidth()) { CaseToolHeader("Профиль", onBack) }
        Spacer(Modifier.height(42.dp))
        Surface(
            modifier = Modifier.size(120.dp),
            shape = CircleShape,
            color = colors.surface2,
            border = BorderStroke(1.dp, colors.outline),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("СД", style = MaterialTheme.typography.displayLarge, color = colors.accent)
            }
        }
        Spacer(Modifier.height(20.dp))
        Text("Следователь", style = MaterialTheme.typography.headlineLarge)
        Text("Ваш прогресс в расследованиях", style = MaterialTheme.typography.bodySmall, color = colors.text2)
        Spacer(Modifier.height(32.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("3" to "Всего дел", "2" to "Активных", "1" to "Раскрыто").forEach { (value, label) ->
                AppCard(Modifier.weight(1f), outlined = true, padding = 8.dp) {
                    Column(
                        Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(value, color = colors.accent, style = MaterialTheme.typography.headlineMedium)
                        Text(label, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        auth.message?.let { Text(it, color = colors.error) }
        AppButton(
            text = if (auth.busy) "Выходим…" else "Выйти из аккаунта",
            onClick = onSignOut,
            enabled = !auth.busy,
            style = AppButtonStyle.Outlined,
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
internal fun InventoryScreen(viewModel: InventoryViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = AppTheme.colors
    var selectedClueId by rememberSaveable { mutableStateOf<Int?>(null) }
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        CaseToolHeader("Инвентарь улик", onBack)
        Text("Материалы дела", style = MaterialTheme.typography.displayLarge)
        Text("Найденные предметы и доказательства.", color = colors.text2, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(20.dp))
        when (val current = state) {
            CluePickerState.Loading -> ScreenProgress()
            CluePickerState.Error -> ScreenRetry("Не удалось загрузить инвентарь.", viewModel::load)
            is CluePickerState.Content -> if (current.clues.isEmpty()) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    AppCard(outlined = true) {
                        Column(
                            Modifier.fillMaxWidth().padding(vertical = 36.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Icon(painterResource(R.drawable.ic_inventory), null, tint = colors.accent)
                            Text("Пока нет улик", style = MaterialTheme.typography.headlineLarge)
                            Text(
                                "Осмотрите место происшествия, чтобы найти предметы.",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = colors.text2,
                            )
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(current.clues, key = { it.id }) { clue ->
                        Surface(
                            onClick = { selectedClueId = clue.id },
                            shape = AppShapes.card,
                            color = colors.surface,
                            border = BorderStroke(1.dp, colors.outline),
                        ) {
                            Column(
                                Modifier.heightIn(min = 140.dp).padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Box(Modifier.size(40.dp).background(colors.surface2, AppShapes.control))
                                Text(clue.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Text(clue.description, style = MaterialTheme.typography.bodySmall, color = colors.text2, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
                current.clues.firstOrNull { it.id == selectedClueId }?.let { clue ->
                    AlertDialog(
                        onDismissRequest = { selectedClueId = null },
                        title = { Text("Материал дела") },
                        text = { ClueCard(clue.title, clue.description) },
                        confirmButton = { AppButton("Закрыть", { selectedClueId = null }) },
                        containerColor = colors.bg,
                    )
                }
            }
        }
    }
}

@Composable
internal fun MorseScreen(onBack: () -> Unit) {
    val colors = AppTheme.colors
    var code by rememberSaveable { mutableStateOf("") }
    var status by rememberSaveable { mutableStateOf<MorseCodeCheck?>(null) }
    val solved = status == MorseCodeCheck.Correct
    val border = when (status) {
        MorseCodeCheck.Incorrect, MorseCodeCheck.Incomplete -> colors.error
        MorseCodeCheck.Correct -> colors.success
        null -> colors.outline
    }

    fun checkCode() {
        if (!solved) status = DemoMorsePuzzle.check(code)
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CaseToolHeader("Головоломка", onBack)
        Text("Восстановите код", style = MaterialTheme.typography.displayLarge)
        Text("Расшифруйте четыре цифры сигнала Морзе.", style = MaterialTheme.typography.bodySmall, color = colors.text2)
        Spacer(Modifier.height(12.dp))
        AppCard(outlined = true) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Код доступа", style = MaterialTheme.typography.bodySmall, color = colors.text2)
                Text(DemoMorsePuzzle.Signal, style = MaterialTheme.typography.headlineMedium)
                Text("Каждая цифра — пять знаков. Для 1–5 сначала идут точки, для 6–0 — тире.", style = MaterialTheme.typography.bodySmall, color = colors.text2)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    repeat(DemoMorsePuzzle.CodeLength) { index ->
                        Surface(
                            modifier = Modifier.weight(1f).height(58.dp),
                            shape = AppShapes.control,
                            color = if (solved) colors.success else colors.surface2,
                            contentColor = if (solved) colors.bg else colors.text,
                            border = BorderStroke(1.dp, border),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(code.getOrNull(index)?.toString() ?: "–", style = MaterialTheme.typography.headlineMedium)
                            }
                        }
                    }
                }
                val feedback = when (status) {
                    MorseCodeCheck.Incomplete -> "Введите все четыре цифры."
                    MorseCodeCheck.Incorrect -> "Неверный код. Проверьте сигнал."
                    MorseCodeCheck.Correct -> "Код принят."
                    null -> null
                }
                feedback?.let { Text(it, color = border, style = MaterialTheme.typography.bodySmall) }
            }
        }
        Column(Modifier.padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "⌫", "0", "✓").chunked(3).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { key ->
                        AppButton(
                            text = key,
                            onClick = {
                                when (key) {
                                    "✓" -> checkCode()
                                    "⌫" -> { code = code.dropLast(1); status = null }
                                    else -> if (code.length < DemoMorsePuzzle.CodeLength) {
                                        code += key
                                        status = null
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            style = if (key == "✓") AppButtonStyle.Primary else AppButtonStyle.Outlined,
                            enabled = !solved,
                        )
                    }
                }
            }
        }
        if (solved) {
            Surface(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                shape = AppShapes.control,
                color = colors.success,
                contentColor = colors.bg,
            ) {
                Text("Вернуться к делу", Modifier.padding(14.dp), textAlign = TextAlign.Center)
            }
        } else {
            AppButton("Проверить код", ::checkCode, style = AppButtonStyle.Accent)
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
internal fun AccusationScreen(
    viewModel: AccusationViewModel,
    onBack: () -> Unit,
    onStatusChanged: (Int, String) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val selected by viewModel.selectedId.collectAsStateWithLifecycle()
    val colors = AppTheme.colors
    var confirming by rememberSaveable { mutableStateOf(false) }
    val result = state.result

    LaunchedEffect(result) {
        result?.let { onStatusChanged(it.caseId, if (it.isCorrect == true) "solved" else "failed") }
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        CaseToolHeader("Обвинение", onBack)
        Text("Кто виновен?", style = MaterialTheme.typography.displayLarge)
        Text("Выберите одного подозреваемого.", style = MaterialTheme.typography.bodySmall, color = colors.text2)
        when {
            state.loading -> ScreenProgress()
            state.error != null && (state.suspects.isEmpty() || state.closed) -> ScreenRetry(state.error.orEmpty(), viewModel::load)
            state.suspects.isEmpty() -> Text("В деле пока нет подозреваемых.", color = colors.text2)
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f).selectableGroup(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.suspects, key = { it.id }) { suspect ->
                        val chosen = suspect.id == selected
                        Surface(
                            modifier = Modifier.fillMaxWidth().clip(AppShapes.card).selectable(
                                selected = chosen,
                                enabled = !state.submitting && !state.closed,
                                role = Role.RadioButton,
                                onClick = { viewModel.select(suspect.id) },
                            ),
                            color = if (chosen) colors.surface2 else colors.surface,
                            shape = AppShapes.card,
                            border = BorderStroke(if (chosen) 2.dp else 1.dp, if (chosen) colors.accent else colors.outline),
                        ) {
                            Column(Modifier.heightIn(min = 154.dp).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(Modifier.size(40.dp).background(colors.primary, AppShapes.control), contentAlignment = Alignment.Center) {
                                    Text(suspect.name.take(1), color = colors.bg)
                                }
                                Text(suspect.name, style = MaterialTheme.typography.titleMedium)
                                Text(suspect.profile, style = MaterialTheme.typography.bodySmall, color = colors.text2, maxLines = 3, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
                state.error?.let { Text(it, color = colors.error) }
                AppButton(
                    text = if (state.submitting) "Проверяем обвинение…" else "Обвинить",
                    onClick = { confirming = true },
                    enabled = state.suspects.any { it.id == selected } && !state.submitting && !state.closed,
                    style = AppButtonStyle.Accent,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
            }
        }
    }

    val chosen = state.suspects.firstOrNull { it.id == selected }
    if (confirming && chosen != null && result == null && !state.closed) {
        AlertDialog(
            onDismissRequest = { confirming = false },
            title = { Text("Подтвердить обвинение") },
            text = { Text("Вы обвиняете персонажа «${chosen.name}». После отправки выбор станет окончательным.") },
            confirmButton = { AppButton("Обвинить", { confirming = false; viewModel.accuse() }, style = AppButtonStyle.Accent) },
            dismissButton = { AppButton("Отмена", { confirming = false }, style = AppButtonStyle.Outlined) },
            containerColor = colors.bg,
        )
    }
    if (result != null) {
        val culprit = state.suspects.firstOrNull { it.id == result.culpritId }
            ?: state.suspects.singleOrNull { result.explanation == "Это ошибка. Преступником был ${it.name}." }
        AlertDialog(
            onDismissRequest = onBack,
            title = { Text(if (result.isCorrect == true) "Дело раскрыто" else "Обвинение ошибочно") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(result.explanation, style = MaterialTheme.typography.bodyMedium)
                    culprit?.let {
                        Text("Настоящий виновник", style = MaterialTheme.typography.bodySmall, color = colors.text2)
                        AppCard(outlined = true) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(it.name, style = MaterialTheme.typography.headlineMedium)
                                Text(it.profile, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            },
            confirmButton = { AppButton("Вернуться к делу", onBack) },
            containerColor = colors.bg,
        )
    }
}
