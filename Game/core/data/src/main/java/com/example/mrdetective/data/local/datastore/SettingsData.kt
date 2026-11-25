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

/** Stores game preferences and player metadata in DataStore. */
@Singleton
class SettingsData
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : VibrationPreferences, CameraPreferences, PuzzlePermissionsPreferences {
        /** Defines the DataStore keys shared by the application's settings. */
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

        /** Stores the user's choice of camera mode for clue puzzles. */
        override suspend fun setCameraEnabled(enabled: Boolean) {
            context.settingsDataStore.edit { it[Keys.CAMERA_ENABLED] = enabled }
        }

        /** Checks whether the permissions introduction has been shown to the specified player. */
        override suspend fun hasSeenPuzzlePermissions(userId: String): Boolean =
            context.settingsDataStore.data.first()[booleanPreferencesKey("puzzle_permissions_seen_$userId")] == true

        /** Records that the specified player has seen the permissions introduction. */
        override suspend fun markPuzzlePermissionsSeen(userId: String) {
            context.settingsDataStore.edit { it[booleanPreferencesKey("puzzle_permissions_seen_$userId")] = true }
        }

        /** Reads the locally cached nickname for a player. */
        suspend fun getNickname(userId: String): String? = context.settingsDataStore.data.first()[stringPreferencesKey("nickname_$userId")]

        /** Updates the locally cached nickname for a player. */
        suspend fun setNickname(
            userId: String,
            nickname: String,
        ) {
            context.settingsDataStore.edit { it[stringPreferencesKey("nickname_$userId")] = nickname }
        }

        /** Reads the saved choice for vibration, if the player has set one. */
        override suspend fun getVibrationEnabled(): Boolean? = context.settingsDataStore.data.first()[Keys.VIBRATION_ENABLED]

        /** Stores the player's choice of whether to use vibration in Morse puzzles. */
        override suspend fun setVibrationEnabled(enabled: Boolean) {
            context.settingsDataStore.edit { it[Keys.VIBRATION_ENABLED] = enabled }
        }

        /** Stores the preferred dark-theme flag. */
        suspend fun setDarkTheme(enabled: Boolean) {
            context.settingsDataStore.edit { it[Keys.DARK_THEME] = enabled }
        }

        /** Stores whether game sound should be enabled. */
        suspend fun setSoundEnabled(enabled: Boolean) {
            context.settingsDataStore.edit { it[Keys.SOUND_ENABLED] = enabled }
        }

        /** Stores the preferred language identifier. */
        suspend fun setLanguage(language: String) {
            context.settingsDataStore.edit { it[Keys.LANGUAGE] = language }
        }

        /** Updates the stored player identifier. */
        suspend fun setUserId(id: String?) {
            context.settingsDataStore.edit { if (id == null) it.remove(Keys.USER_ID) else it[Keys.USER_ID] = id }
        }

        /** Updates the identifier of the last case opened. */
        suspend fun setLastOpenedCase(id: Int?) {
            context.settingsDataStore.edit { if (id == null) it.remove(Keys.LAST_OPENED_CASE) else it[Keys.LAST_OPENED_CASE] = id }
        }
    }
