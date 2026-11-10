package com.example.mrdetective.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.mrdetective.presentation.ui.theme.AppTheme
import com.example.mrdetective.presentation.ui.theme.ColdTrailTheme
import com.example.mrdetective.presentation.ui.theme.Spacing

@Composable
private fun DesignSystemShowcase() {
    var query by remember { mutableStateOf("") }
    Column(
        modifier =
            Modifier
                .background(AppTheme.colors.bg)
                .padding(Spacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Text("Холодный след", style = MaterialTheme.typography.displayLarge, color = AppTheme.colors.text)
        Text("Заголовок экрана", style = MaterialTheme.typography.headlineLarge, color = AppTheme.colors.text)
        Text(
            "Основной текст: описание дела и реплики подозреваемых.",
            style = MaterialTheme.typography.bodyLarge,
            color = AppTheme.colors.text,
        )
        AppTextField(value = query, onValueChange = { query = it }, placeholder = "Задайте вопрос...")
        AppTextField(value = "Где вы были в 22:14?", onValueChange = {}, placeholder = "")
        AppButton("Основная кнопка", {})
        AppButton("Акцентная кнопка", {}, style = AppButtonStyle.Accent)
        AppButton("Контурная кнопка", {}, style = AppButtonStyle.Outlined)
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            ChipStatus.entries.forEach { Statuses(it) }
        }
        CaseCard("Дело №01: Пропажа", "В музее исчез экспонат. Камера не работала в 22:14.", {})
        SuspectCard("Марк Волков", "Охранник музея", {})
        ClueCard("Записка", "Найдена у чёрного входа.")
    }
}

@Preview(name = "Светлая тема", showBackground = true, heightDp = 1200)
@Composable
private fun ShowcaseLightPreview() = ColdTrailTheme(darkTheme = false) { DesignSystemShowcase() }

@Preview(name = "Тёмная тема", showBackground = true, heightDp = 1200)
@Composable
private fun ShowcaseDarkPreview() = ColdTrailTheme(darkTheme = true) { DesignSystemShowcase() }
