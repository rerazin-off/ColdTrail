package com.example.mrdetective.presentation.ui

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.StringRes
import com.example.mrdetective.presentation.R
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject

/** Resolves localized resources for state and errors produced outside Compose. */
class UiStrings
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
    ) {
        private val domainMessages by lazy {
            val russian = Configuration(context.resources.configuration).apply { setLocale(Locale.forLanguageTag("ru")) }
            val source = context.createConfigurationContext(russian).resources
            listOf(
                R.string.email_invalid,
                R.string.password_required,
                R.string.password_too_short,
                R.string.four_digits_required,
                R.string.wait_for_sync,
                R.string.investigation_not_started,
                R.string.direction_sensor_unavailable,
                R.string.direction_sensor_error,
                R.string.rear_camera_error,
                R.string.network_server,
                R.string.background_sync_error,
            ).associateBy { source.getString(it) }
        }

        /** Reads a formatted string resource for the current app locale. */
        fun get(
            @StringRes resource: Int,
            vararg arguments: Any,
        ): String = if (arguments.isEmpty()) context.getString(resource) else context.getString(resource, *arguments)

        /** Localizes recognized domain feedback and preserves other messages. */
        fun domainMessage(
            message: String?,
            fallback: String,
        ): String = domainMessages[message]?.let { get(it) } ?: fallback
    }
