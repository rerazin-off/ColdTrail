package com.example.mrdetective.data.network

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Persists the Supabase session and restores it for authenticated requests. */
@Singleton
class SessionTokenStore
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) {
        private val preferences =
            EncryptedSharedPreferences.create(
                context,
                "coldtrail_secure_session",
                MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )

        private var generation = 0L

        /** Holds the access token, refresh token, and associated player identifier. */
        internal data class Session(val generation: Long, val access: String, val refresh: String?)

        @Synchronized
        internal fun session(): Session? =
            preferences.getString(KEY_ACCESS, null)?.takeIf(String::isNotBlank)?.let {
                Session(generation, it, preferences.getString(KEY_REFRESH, null))
            }

        /** Saves the session and makes it available to subsequent requests. */
        @Synchronized
        fun setSession(
            accessToken: String,
            refreshToken: String?,
        ) {
            generation++
            writeTokens(accessToken, refreshToken)
        }

        @Synchronized
        internal fun updateSession(
            expected: Session,
            accessToken: String?,
            refreshToken: String?,
        ): Boolean {
            if (session() != expected) return false
            writeTokens(accessToken, refreshToken)
            return true
        }

        private fun writeTokens(
            accessToken: String?,
            refreshToken: String?,
        ) {
            preferences.edit()
                .putString(KEY_ACCESS, accessToken)
                .putString(KEY_REFRESH, refreshToken)
                .apply()
        }

        /** Removes the persisted account and token information. */
        @Synchronized
        fun clear() {
            generation++
            writeTokens(null, null)
        }

        /** Defines the shared access to the persistent session store. */
        private companion object {
            const val KEY_ACCESS = "access_token"
            const val KEY_REFRESH = "refresh_token"
        }
    }
