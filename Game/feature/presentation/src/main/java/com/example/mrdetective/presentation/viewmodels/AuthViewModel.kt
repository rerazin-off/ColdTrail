package com.example.mrdetective.presentation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.repositories.AuthRepository
import com.example.mrdetective.domain.usecases.auth.SignInUseCase
import com.example.mrdetective.domain.usecases.auth.SignUpUseCase
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
                AuthUiState(checkingSession = false, message = "Не удалось восстановить сеанс. Войдите снова.")
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
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(address).matches()) {
            mutableState.value = state.value.copy(message = "Введите корректный адрес почты.", confirmationRequired = false)
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
                    error is DomainError.Network -> "Не удалось связаться с сервером. Проверьте подключение."
                    password.isBlank() -> "Введите пароль."
                    register && password.length < 6 -> "Пароль должен содержать не менее 6 символов."
                    error is DomainError.Validation || error is DomainError.Unauthorized ->
                        if (register) "Не удалось создать аккаунт. Проверьте почту и пароль."
                        else "Проверьте почту, пароль и подтверждение адреса."
                    else -> "Не удалось выполнить запрос. Попробуйте ещё раз."
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
                AuthUiState(checkingSession = false, signedIn = true, message = "Не удалось выйти. Попробуйте ещё раз.")
            }
        }
    }
}
