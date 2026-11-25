package com.example.mrdetective.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mrdetective.presentation.R
import com.example.mrdetective.presentation.ui.components.AppButton
import com.example.mrdetective.presentation.ui.components.AppButtonStyle
import com.example.mrdetective.presentation.ui.components.AppCard
import com.example.mrdetective.presentation.ui.components.AppErrorDialog
import com.example.mrdetective.presentation.ui.components.AppTextField
import com.example.mrdetective.presentation.ui.theme.AppTheme
import com.example.mrdetective.presentation.ui.theme.ColdTrailTheme

@Composable
internal fun AuthScreen(
    state: AuthUiState,
    onSubmit: (String, String, Boolean) -> Unit,
    onClearMessage: () -> Unit,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var register by rememberSaveable { mutableStateOf(false) }
    val colors = AppTheme.colors

    LaunchedEffect(state.confirmationRequired) {
        if (state.confirmationRequired) {
            register = false
            password = ""
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize().imePadding(), contentAlignment = Alignment.TopCenter) {
        val availableHeight = maxHeight
        Column(
            modifier =
                Modifier.widthIn(max = 460.dp).verticalScroll(rememberScrollState())
                    .heightIn(min = availableHeight).padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        ) {
            Text(stringResource(R.string.app_title), style = MaterialTheme.typography.displayLarge)
            AppCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        if (register) stringResource(R.string.registration) else stringResource(R.string.sign_in_title),
                        style = MaterialTheme.typography.headlineLarge,
                    )
                    AppTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            onClearMessage()
                        },
                        placeholder = stringResource(R.string.email),
                        enabled = !state.busy,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    )
                    AppTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            onClearMessage()
                        },
                        placeholder = stringResource(R.string.password),
                        enabled = !state.busy,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = PasswordVisualTransformation(),
                    )
                    if (state.confirmationRequired) {
                        Text(
                            stringResource(R.string.confirm_email),
                            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                            color = colors.success,
                        )
                    }
                    state.message?.let {
                        AppErrorDialog(it, onClose = onClearMessage, onRetry = { onSubmit(email, password, register) })
                    }
                    AppButton(
                        text =
                            when {
                                state.busy ->
                                    if (register) {
                                        stringResource(
                                            R.string.creating_account,
                                        )
                                    } else {
                                        stringResource(R.string.signing_in)
                                    }
                                register -> stringResource(R.string.register)
                                else -> stringResource(R.string.sign_in)
                            },
                        onClick = { onSubmit(email, password, register) },
                        enabled = !state.busy,
                    )
                    AppButton(
                        text = if (register) stringResource(R.string.existing_account) else stringResource(R.string.create_account),
                        onClick = {
                            register = !register
                            password = ""
                            onClearMessage()
                        },
                        enabled = !state.busy,
                        style = AppButtonStyle.Outlined,
                    )
                }
            }
        }
    }
}

@Preview(name = "Светлая тема", widthDp = 390, heightDp = 760)
@Preview(name = "Тёмная тема", widthDp = 390, heightDp = 760, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AuthPreview() {
    ColdTrailTheme {
        Surface(color = AppTheme.colors.bg, contentColor = AppTheme.colors.text) {
            AuthScreen(AuthUiState(checkingSession = false), onSubmit = { _, _, _ -> }, onClearMessage = {})
        }
    }
}
