package com.example.mrdetective.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.mrdetective.domain.hardware.CameraPreferences
import com.example.mrdetective.domain.hardware.PuzzlePermissionsPreferences
import com.example.mrdetective.domain.hardware.VibrationPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore by preferencesDataStore(name = "coldtrail_settings")

/** Хранит настройки игры и данные игрока в DataStore. */
@Singleton
class SettingsData
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : VibrationPreferences, CameraPreferences, PuzzlePermissionsPreferences {
        /** Определяет ключи DataStore для настроек приложения. */
        private object Keys {
            val DARK_THEME = booleanPreferencesKey("dark_theme")
            val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
            val LANGUAGE = stringPreferencesKey("language")
            val LAST_OPENED_CASE = androidx.datastore.preferences.core.intPreferencesKey("last_opened_case")
            val USER_ID = stringPreferencesKey("user_id")
            val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
            val CAMERA_ENABLED = booleanPreferencesKey("camera_enabled")
        }

        val darkTheme: Flow<Boolean> = context.settingsDataStore.data.map { it[Keys.DARK_THEME] ?: false }
        val soundEnabled: Flow<Boolean> = context.settingsDataStore.data.map { it[Keys.SOUND_ENABLED] ?: true }
        val language: Flow<String> = context.settingsDataStore.data.map { it[Keys.LANGUAGE] ?: "ru" }
        val lastOpenedCaseId: Flow<Int?> = context.settingsDataStore.data.map { it[Keys.LAST_OPENED_CASE] }
        val userId: Flow<String?> = context.settingsDataStore.data.map { it[Keys.USER_ID] }
        override val cameraEnabled: Flow<Boolean?> = context.settingsDataStore.data.map { it[Keys.CAMERA_ENABLED] }

        /** Сохраняет выбор игрока об использовании камеры в головоломках с уликами. */
        override suspend fun setCameraEnabled(enabled: Boolean) {
            context.settingsDataStore.edit { it[Keys.CAMERA_ENABLED] = enabled }
        }

        /** Проверяет, был ли показан экран разрешений указанному игроку. */
        override suspend fun hasSeenPuzzlePermissions(userId: String): Boolean =
            context.settingsDataStore.data.first()[booleanPreferencesKey("puzzle_permissions_seen_$userId")] == true

        /** Отмечает, что указанный игрок увидел экран разрешений. */
        override suspend fun markPuzzlePermissionsSeen(userId: String) {
            context.settingsDataStore.edit { it[booleanPreferencesKey("puzzle_permissions_seen_$userId")] = true }
        }

        /** Читает сохранённый на устройстве никнейм игрока. */
        suspend fun getNickname(userId: String): String? = context.settingsDataStore.data.first()[stringPreferencesKey("nickname_$userId")]

        /** Обновляет сохранённый на устройстве никнейм игрока. */
        suspend fun setNickname(
            userId: String,
            nickname: String,
        ) {
            context.settingsDataStore.edit { it[stringPreferencesKey("nickname_$userId")] = nickname }
        }

        /** Читает настройку вибрации, если игрок уже её задал. */
        override suspend fun getVibrationEnabled(): Boolean? = context.settingsDataStore.data.first()[Keys.VIBRATION_ENABLED]

        /** Сохраняет выбор игрока об использовании вибрации в головоломках Морзе. */
        override suspend fun setVibrationEnabled(enabled: Boolean) {
            context.settingsDataStore.edit { it[Keys.VIBRATION_ENABLED] = enabled }
        }

        /** Сохраняет настройку тёмной темы. */
        suspend fun setDarkTheme(enabled: Boolean) {
            context.settingsDataStore.edit { it[Keys.DARK_THEME] = enabled }
        }

        /** Сохраняет настройку звука игры. */
        suspend fun setSoundEnabled(enabled: Boolean) {
            context.settingsDataStore.edit { it[Keys.SOUND_ENABLED] = enabled }
        }

        /** Сохраняет идентификатор выбранного языка. */
        suspend fun setLanguage(language: String) {
            context.settingsDataStore.edit { it[Keys.LANGUAGE] = language }
        }

        /** Обновляет сохранённый идентификатор игрока. */
        suspend fun setUserId(id: String?) {
            context.settingsDataStore.edit { if (id == null) it.remove(Keys.USER_ID) else it[Keys.USER_ID] = id }
        }

        /** Обновляет идентификатор последнего открытого дела. */
        suspend fun setLastOpenedCase(id: Int?) {
            context.settingsDataStore.edit { if (id == null) it.remove(Keys.LAST_OPENED_CASE) else it[Keys.LAST_OPENED_CASE] = id }
        }
    }
