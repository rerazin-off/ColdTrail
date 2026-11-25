package com.example.mrdetective.presentation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mrdetective.domain.hardware.CameraPreferences
import com.example.mrdetective.domain.hardware.VibrationPreferences
import com.example.mrdetective.domain.repositories.ProfileStatistics
import com.example.mrdetective.domain.repositories.ProfileStatisticsRepository
import com.example.mrdetective.domain.usecases.puzzles.PlayMorseSignalUseCase
import com.example.mrdetective.presentation.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

/** Identifies the route that displays device permissions and preferences. */
@Serializable
internal object DeviceSettingsRoute

/** Tracks vibration availability, preference, and playback feedback. */
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

/** Loads and updates the vibration preference and plays Morse signals. */
@HiltViewModel
internal class VibrationViewModel
    @Inject
    constructor(
        private val strings: UiStrings,
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

        /** Reads the stored preference and checks whether vibration is available. */
        fun load() {
            if (loadJob?.isActive == true || state.value.saving) return
            mutableState.update { it.copy(loading = true, settingsError = null) }
            loadJob =
                viewModelScope.launch {
                    try {
                        val enabled = preferences.getVibrationEnabled()
                        mutableState.update { it.copy(enabled = enabled, loading = false) }
                    } catch (error: CancellationException) {
                        throw error
                    } catch (_: Exception) {
                        mutableState.update { it.copy(loading = false, settingsError = strings.get(R.string.read_vibration_error)) }
                    }
                }
        }

        /** Persists the player's current preference for vibration. */
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
                    mutableState.update { it.copy(saving = false, settingsError = strings.get(R.string.save_vibration_error)) }
                }
            }
        }

        /** Plays the Morse pattern and reports playback state. */
        fun play(signal: String) {
            if (!state.value.usesVibration || state.value.saving || playbackJob?.isActive == true) return
            playbackJob =
                viewModelScope.launch {
                    mutableState.update { it.copy(playing = true) }
                    try {
                        playSignal(signal).getOrThrow()
                    } catch (error: CancellationException) {
                        throw error
                    } catch (_: Exception) {
                        mutableState.update { it.copy(playbackError = strings.get(R.string.vibration_playback_error)) }
                    } finally {
                        mutableState.update { it.copy(playing = false) }
                    }
                }
        }

        /** Resets the playback feedback when the player leaves the Morse screen. */
        fun stopPlayback() {
            playbackJob?.cancel()
        }
    }

/** Tracks the player's camera preference in the permissions UI. */
internal data class CameraSettingsState(
    val enabled: Boolean? = null,
    val loading: Boolean = true,
    val saving: Boolean = false,
    val error: String? = null,
)

/** Loads and updates the player's camera preference. */
@HiltViewModel
internal class CameraSettingsViewModel
    @Inject
    constructor(
        private val strings: UiStrings,
        private val preferences: CameraPreferences,
    ) : ViewModel() {
        private val mutableState = MutableStateFlow(CameraSettingsState())
        val state = mutableState.asStateFlow()
        private var preferenceJob: Job? = null

        init {
            load()
        }

        /** Reads the saved preference for camera puzzles. */
        fun load() {
            if (preferenceJob?.isActive == true) {
                mutableState.update { it.copy(error = null) }
                return
            }
            mutableState.update { it.copy(loading = true, error = null) }
            preferenceJob =
                viewModelScope.launch {
                    try {
                        preferences.cameraEnabled.collect { enabled ->
                            mutableState.update { it.copy(enabled = enabled, loading = false) }
                        }
                    } catch (error: CancellationException) {
                        throw error
                    } catch (_: Exception) {
                        mutableState.update { it.copy(loading = false, error = strings.get(R.string.read_camera_error)) }
                    }
                }
        }

        /** Persists the choice of whether a clue puzzle can use the device camera. */
        fun setEnabled(enabled: Boolean) {
            if (state.value.saving) return
            mutableState.update { it.copy(saving = true, error = null) }
            viewModelScope.launch {
                try {
                    preferences.setCameraEnabled(enabled)
                    mutableState.update { it.copy(enabled = enabled, saving = false) }
                    load()
                } catch (error: CancellationException) {
                    throw error
                } catch (_: Exception) {
                    mutableState.update { it.copy(saving = false, error = strings.get(R.string.save_camera_error)) }
                }
            }
        }
    }

/** Represents loading, failure, or loaded profile statistics. */
internal sealed interface ProfileStatisticsState {
    /** Indicates that the profile statistics are being retrieved. */
    data object Loading : ProfileStatisticsState

    /** Holds the message shown when profile statistics cannot be retrieved. */
    data class Error(val message: String) : ProfileStatisticsState

    /** Holds the statistics shown on the player's profile. */
    data class Content(val statistics: ProfileStatistics) : ProfileStatisticsState
}

/** Loads profile statistics and the player's nickname. */
@HiltViewModel
internal class ProfileStatisticsViewModel
    @Inject
    constructor(
        private val strings: UiStrings,
        private val repository: ProfileStatisticsRepository,
    ) : ViewModel() {
        private val mutableState = MutableStateFlow<ProfileStatisticsState>(ProfileStatisticsState.Loading)
        val state = mutableState.asStateFlow()
        private val mutableNickname = MutableStateFlow<String?>(null)
        val nickname = mutableNickname.asStateFlow()
        private val mutableNicknameError = MutableStateFlow<String?>(null)
        val nicknameError = mutableNicknameError.asStateFlow()
        private var loadJob: Job? = null
        private var nicknameJob: Job? = null

        /** Loads the nickname for display in the player profile. */
        fun loadNickname() {
            if (nicknameJob?.isActive == true) return
            mutableNicknameError.value = null
            nicknameJob =
                viewModelScope.launch {
                    repository.loadNickname().fold(
                        { mutableNickname.value = it },
                        { mutableNicknameError.value = it.uiMessage(strings, strings.get(R.string.nickname_load_error)) },
                    )
                }
        }

        /** Loads the current player's investigation statistics. */
        fun load() {
            if (loadJob?.isActive == true) return
            mutableState.value = ProfileStatisticsState.Loading
            loadJob =
                viewModelScope.launch {
                    mutableState.value =
                        try {
                            ProfileStatisticsState.Content(repository.load().getOrThrow())
                        } catch (error: CancellationException) {
                            throw error
                        } catch (error: Exception) {
                            ProfileStatisticsState.Error(error.uiMessage(strings, strings.get(R.string.statistics_load_error)))
                        }
                }
        }
    }
