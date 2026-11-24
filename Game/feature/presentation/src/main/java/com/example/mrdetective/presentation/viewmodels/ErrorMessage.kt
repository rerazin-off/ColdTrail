package com.example.mrdetective.presentation.ui

import com.example.mrdetective.presentation.R
import com.example.mrdetective.domain.models.DomainError

internal fun Throwable.uiMessage(strings: UiStrings, fallback: String): String = when (this) {
    is DomainError.Network -> when (kind) {
        DomainError.Network.Kind.Offline -> strings.get(R.string.network_offline)
        DomainError.Network.Kind.Server -> strings.get(R.string.network_server)
        DomainError.Network.Kind.Connection -> strings.get(R.string.network_connection)
    }
    is DomainError.SyncPending -> strings.get(R.string.sync_pending)
    is DomainError.Unauthorized -> strings.get(R.string.session_unavailable)
    else -> fallback
}
