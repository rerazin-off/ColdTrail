package com.example.mrdetective.presentation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mrdetective.domain.hardware.VibrationPreferences
import com.example.mrdetective.domain.repositories.ProfileStatistics
import com.example.mrdetective.domain.repositories.ProfileStatisticsRepository
import com.example.mrdetective.domain.usecases.puzzles.PlayMorseSignalUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
internal object DeviceSettingsRoute

internal data class VibrationState(
    val available: Boolean,
    val enabled: Boolean? = null,
    val loading: Boolean = true,
    val saving: Boolean = false,
    val playing: Boolean = false,
    val settingsError: String? = null,
    val playbackError: String? = null,
) {
    val usesVibration: Boolean
        get() = available && enabled == true && playbackError == null
}

@HiltViewModel
internal class VibrationViewModel @Inject constructor(
    private val preferences: VibrationPreferences,
    private val playSignal: PlayMorseSignalUseCase,
) : ViewModel() {
    private val mutableState = MutableStateFlow(VibrationState(available = playSignal.isAvailable()))
    val state = mutableState.asStateFlow()
    private var loadJob: Job? = null
    private var playbackJob: Job? = null

    init {
        load()
    }

    fun load() {
        if (loadJob?.isActive == true || state.value.saving) return
        mutableState.update { it.copy(loading = true, settingsError = null) }
        loadJob = viewModelScope.launch {
            try {
                val enabled = preferences.getVibrationEnabled()
                mutableState.update { it.copy(enabled = enabled, loading = false) }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                mutableState.update { it.copy(loading = false, settingsError = "Не удалось прочитать настройку вибрации.") }
            }
        }
    }

    fun setEnabled(enabled: Boolean) {
        val current = state.value
        if (current.loading || current.saving || (enabled && !current.available)) return
        if (!enabled) stopPlayback()
        mutableState.update { it.copy(saving = true, settingsError = null) }
        viewModelScope.launch {
            try {
                preferences.setVibrationEnabled(enabled)
                mutableState.update { it.copy(enabled = enabled, saving = false, playbackError = null) }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                mutableState.update { it.copy(saving = false, settingsError = "Не удалось сохранить настройку вибрации.") }
            }
        }
    }

    fun play(signal: String) {
        if (!state.value.usesVibration || state.value.saving || playbackJob?.isActive == true) return
        playbackJob = viewModelScope.launch {
            mutableState.update { it.copy(playing = true) }
            try {
                playSignal(signal).getOrThrow()
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                mutableState.update { it.copy(playbackError = "Вибрация не сработала. Сигнал показан на экране.") }
            } finally {
                mutableState.update { it.copy(playing = false) }
            }
        }
    }

    fun stopPlayback() {
        playbackJob?.cancel()
    }
}

internal sealed interface ProfileStatisticsState {
    data object Loading : ProfileStatisticsState
    data object Error : ProfileStatisticsState
    data class Content(val statistics: ProfileStatistics) : ProfileStatisticsState
}

@HiltViewModel
internal class ProfileStatisticsViewModel @Inject constructor(
    private val repository: ProfileStatisticsRepository,
) : ViewModel() {
    private val mutableState = MutableStateFlow<ProfileStatisticsState>(ProfileStatisticsState.Loading)
    val state = mutableState.asStateFlow()
    private var loadJob: Job? = null

    fun load() {
        if (loadJob?.isActive == true) return
        mutableState.value = ProfileStatisticsState.Loading
        loadJob = viewModelScope.launch {
            mutableState.value = try {
                ProfileStatisticsState.Content(repository.load().getOrThrow())
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                ProfileStatisticsState.Error
            }
        }
    }
}
