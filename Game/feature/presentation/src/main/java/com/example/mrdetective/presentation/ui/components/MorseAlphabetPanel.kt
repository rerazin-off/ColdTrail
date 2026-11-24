package com.example.mrdetective.presentation.ui.components

import androidx.compose.ui.res.stringResource
import com.example.mrdetective.presentation.R
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.example.mrdetective.domain.usecases.puzzles.MorseAlphabet
import com.example.mrdetective.presentation.ui.theme.AppTheme

@Composable
internal fun MorseAlphabetPanel() {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val stateLabel = if (expanded) stringResource(R.string.expanded) else stringResource(R.string.collapsed)
    val colors = AppTheme.colors
    AppCard(outlined = true, padding = 0.dp) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .semantics { stateDescription = stateLabel }
                    .clickable(role = Role.Button) { expanded = !expanded }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(stringResource(R.string.morse_alphabet), style = MaterialTheme.typography.titleMedium)
                Text(if (expanded) "▴" else "▾", color = colors.text2)
            }
            AnimatedVisibility(expanded) {
                Column(
                    Modifier.padding(start = 16.dp, end = 16.dp, bottom = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(stringResource(R.string.latin_letters), style = MaterialTheme.typography.bodySmall, color = colors.text2)
                    MorseAlphabetEntries(MorseAlphabet.letters)
                    Text(stringResource(R.string.digits), style = MaterialTheme.typography.bodySmall, color = colors.text2)
                    MorseAlphabetEntries(MorseAlphabet.digits)
                }
            }
        }
    }
}

@Composable
private fun MorseAlphabetEntries(entries: List<Pair<Char, String>>) {
    val columns = if (LocalDensity.current.fontScale >= 1.4f) 2 else 3
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        entries.chunked(columns).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { (symbol, code) ->
                    Text(
                        "$symbol  $code",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                    )
                }
                repeat(columns - row.size) { Box(Modifier.weight(1f)) }
            }
        }
    }
}
