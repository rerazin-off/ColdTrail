package com.example.mrdetective.presentation.ui

import com.example.mrdetective.presentation.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.repositories.AuthRepository
import com.example.mrdetective.domain.usecases.auth.SignInUseCase
import com.example.mrdetective.domain.usecases.auth.SignUpUseCase
import com.example.mrdetective.domain.usecases.auth.validateAuthCredentials
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val checkingSession: Boolean = true,
    val signedIn: Boolean = false,
    val busy: Boolean = false,
    val message: String? = null,
    val confirmationRequired: Boolean = false,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val strings: UiStrings,
    private val auth: AuthRepository,
    private val signIn: SignInUseCase,
    private val signUp: SignUpUseCase,
) : ViewModel() {
    private val mutableState = MutableStateFlow(AuthUiState())
    val state = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            val restored = try {
                AuthUiState(checkingSession = false, signedIn = auth.currentUser() != null)
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                AuthUiState(checkingSession = false, message = strings.get(R.string.session_restore_error))
            }
            delay(650)
            mutableState.value = restored
        }
    }

    fun clearMessage() {
        if (!state.value.busy) {
            mutableState.value = state.value.copy(message = null, confirmationRequired = false)
        }
    }

    fun submit(email: String, password: String, register: Boolean) {
        if (state.value.busy || state.value.signedIn || state.value.checkingSession) return
        val address = email.trim()
        validateAuthCredentials(address, password, register)?.let { validationError ->
            mutableState.value = state.value.copy(message = strings.domainMessage(validationError, strings.get(R.string.credentials_error)), confirmationRequired = false)
            return
        }
        mutableState.value = state.value.copy(busy = true, message = null, confirmationRequired = false)
        viewModelScope.launch {
            mutableState.value = try {
                val result = if (register) signUp(address, password) else signIn(address, password)
                result.getOrThrow()
                val hasSession = !register || auth.currentUser() != null
                AuthUiState(checkingSession = false, signedIn = hasSession, confirmationRequired = !hasSession)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                val message = when {
                    error is DomainError.Network -> error.uiMessage(strings, strings.get(R.string.sign_in_error))
                    error is DomainError.Validation || error is DomainError.Unauthorized ->
                        if (register) strings.get(R.string.sign_up_error)
                        else strings.get(R.string.credentials_error)
                    else -> strings.get(R.string.request_error)
                }
                AuthUiState(checkingSession = false, message = message)
            }
        }
    }

    fun signOut() {
        if (state.value.busy || !state.value.signedIn) return
        mutableState.value = state.value.copy(busy = true, message = null)
        viewModelScope.launch {
            mutableState.value = try {
                auth.signOut()
                AuthUiState(checkingSession = false)
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                AuthUiState(checkingSession = false, signedIn = true, message = strings.get(R.string.sign_out_error))
            }
        }
    }
}
