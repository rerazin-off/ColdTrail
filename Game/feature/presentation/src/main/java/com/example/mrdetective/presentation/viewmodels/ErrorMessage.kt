package com.example.mrdetective.presentation.ui

import com.example.mrdetective.domain.models.DomainError

internal fun Throwable.uiMessage(fallback: String): String = when (this) {
    is DomainError.Network -> when (kind) {
        DomainError.Network.Kind.Offline -> "Нет подключения к интернету. Проверьте сеть и повторите."
        DomainError.Network.Kind.Server -> "Сервер временно недоступен. Попробуйте ещё раз позже."
        DomainError.Network.Kind.Connection -> "Не удалось связаться с сервером. Проверьте подключение и повторите."
    }
    is DomainError.SyncPending -> "Данные сохранены на телефоне. Синхронизация продолжится при подключении к интернету."
    is DomainError.Unauthorized -> "Сеанс недоступен. Войдите в аккаунт повторно."
    else -> fallback
}
