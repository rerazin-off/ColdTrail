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

/** Обозначает маршрут к экрану разрешений и настроек устройства. */
@Serializable
internal object DeviceSettingsRoute

/**
 * Хранит доступность вибрации, настройку игрока и состояние воспроизведения.
 *
 * @property available Поддерживает ли устройство необходимую возможность.
 * @property enabled Сохранённый выбор игрока. `null` означает, что настройку ещё не задавали.
 * @property loading Выполняется ли первоначальная или повторная загрузка данных.
 * @property saving Выполняется ли запись выбранной настройки.
 * @property playing Воспроизводится ли сигнал Морзе.
 * @property settingsError Ошибка чтения или сохранения настройки, если она произошла.
 * @property playbackError Ошибка воспроизведения вибрации, если она произошла.
 */
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

/**
 * Загружает и меняет настройку вибрации, воспроизводит сигналы Морзе.
 *
 * @param strings Локализованные строки для сообщений и состояний экрана.
 * @param preferences Хранилище настройки, которую меняет данный экран.
 * @param playSignal Сценарий проверки и воспроизведения сигнала Морзе.
 */
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

        /** Читает настройку игрока и проверяет доступность вибрации. */
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

        /** Сохраняет выбранную игроком настройку вибрации. */
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

        /** Воспроизводит сигнал Морзе и показывает состояние воспроизведения. */
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

        /** Сбрасывает индикацию воспроизведения при уходе с экрана Морзе. */
        fun stopPlayback() {
            playbackJob?.cancel()
        }
    }

/**
 * Хранит настройку камеры для экрана разрешений.
 *
 * @property enabled Сохранённый выбор игрока. `null` означает, что настройку ещё не задавали.
 * @property loading Выполняется ли первоначальная или повторная загрузка данных.
 * @property saving Выполняется ли запись выбранной настройки.
 * @property error Причина ошибки, если она возникла.
 */
internal data class CameraSettingsState(
    val enabled: Boolean? = null,
    val loading: Boolean = true,
    val saving: Boolean = false,
    val error: String? = null,
)

/**
 * Загружает и изменяет настройку камеры игрока.
 *
 * @param strings Локализованные строки для сообщений и состояний экрана.
 * @param preferences Хранилище настройки, которую меняет данный экран.
 */
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

        /** Читает сохранённую настройку камеры для головоломок. */
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

        /** Сохраняет выбор игрока об использовании камеры устройства в головоломках. */
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

/** Отражает загрузку, ошибку или результат получения статистики профиля. */
internal sealed interface ProfileStatisticsState {
    /** Обозначает загрузку статистики профиля. */
    data object Loading : ProfileStatisticsState

    /**
     * Хранит сообщение об ошибке загрузки статистики профиля.
     *
     * @property message Сообщение, которое описывает состояние или ошибку.
     */
    data class Error(val message: String) : ProfileStatisticsState

    /**
     * Хранит статистику для отображения в профиле игрока.
     *
     * @property statistics Сводка по расследованиям игрока.
     */
    data class Content(val statistics: ProfileStatistics) : ProfileStatisticsState
}

/**
 * Загружает статистику профиля и никнейм игрока.
 *
 * @param strings Локализованные строки для сообщений и состояний экрана.
 * @param repository Репозиторий статистики расследований и никнейма игрока.
 */
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

        /** Загружает никнейм для отображения в профиле игрока. */
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

        /** Загружает статистику расследований текущего игрока. */
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
