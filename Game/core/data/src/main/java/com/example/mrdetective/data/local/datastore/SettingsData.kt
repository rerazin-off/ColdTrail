package com.example.mrdetective.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "coldtrail_settings")

@Singleton
class SettingsData @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val LANGUAGE = stringPreferencesKey("language")
        val LAST_OPENED_CASE = androidx.datastore.preferences.core.intPreferencesKey("last_opened_case")
        val USER_ID = stringPreferencesKey("user_id")
    }

    val darkTheme: Flow<Boolean> = context.settingsDataStore.data.map { it[Keys.DARK_THEME] ?: false }
    val soundEnabled: Flow<Boolean> = context.settingsDataStore.data.map { it[Keys.SOUND_ENABLED] ?: true }
    val language: Flow<String> = context.settingsDataStore.data.map { it[Keys.LANGUAGE] ?: "ru" }
    val lastOpenedCaseId: Flow<Int?> = context.settingsDataStore.data.map { it[Keys.LAST_OPENED_CASE] }
    val userId: Flow<String?> = context.settingsDataStore.data.map { it[Keys.USER_ID] }

    suspend fun setDarkTheme(enabled: Boolean) { context.settingsDataStore.edit { it[Keys.DARK_THEME] = enabled } }
    suspend fun setSoundEnabled(enabled: Boolean) { context.settingsDataStore.edit { it[Keys.SOUND_ENABLED] = enabled } }
    suspend fun setLanguage(language: String) { context.settingsDataStore.edit { it[Keys.LANGUAGE] = language } }
    suspend fun setUserId(id: String?) { context.settingsDataStore.edit { if (id == null) it.remove(Keys.USER_ID) else it[Keys.USER_ID] = id } }
    suspend fun setLastOpenedCase(id: Int?) {
        context.settingsDataStore.edit { if (id == null) it.remove(Keys.LAST_OPENED_CASE) else it[Keys.LAST_OPENED_CASE] = id }
    }
}
