package com.example.mrdetective.domain.models

import kotlin.coroutines.cancellation.CancellationException

/**
 * Определяет общие категории ошибок для домена, слоя данных и интерфейса.
 *
 * @param message Сообщение, которое описывает состояние или ошибку.
 * @param cause Исходное исключение, если известна причина сбоя.
 */
sealed class DomainError(
    message: String? = null,
    cause: Throwable? = null,
) : Exception(message, cause) {
    /**
     * Обозначает ошибку ввода или игрового действия, которую может исправить игрок.
     *
     * @param message Сообщение, которое описывает состояние или ошибку.
     */
    class Validation(message: String) : DomainError(message)

    /**
     * Обозначает отсутствие запрошенных игровых данных.
     *
     * @param message Сообщение, которое описывает состояние или ошибку.
     */
    class NotFound(message: String = "Не найдено") : DomainError(message)

    /**
     * Обозначает отсутствие или истечение авторизованной сессии.
     *
     * @param message Сообщение, которое описывает состояние или ошибку.
     */
    class Unauthorized(message: String = "Нет доступа") : DomainError(message)

    /**
     * Представляет сетевой сбой и его категорию.
     *
     * @param cause Исходное исключение, если известна причина сбоя.
     * @property kind Категория сетевого сбоя для выбора понятного сообщения игроку.
     */
    class Network(
        cause: Throwable? = null,
        val kind: Kind = Kind.Connection,
    ) : DomainError("Ошибка сети", cause) {
        /** Различает отсутствие сети, сбой сервера и другие сбои соединения. */
        enum class Kind { Offline, Server, Timeout, Connection }
    }

    /** Обозначает операцию, ожидающую синхронизации. */
    class SyncPending : DomainError("Данные ожидают синхронизации")

    /** Обозначает недоступность нужного оборудования устройства. */
    class HardwareUnavailable : DomainError("Вибрация недоступна на этом устройстве")

    /**
     * Сохраняет причину ошибки, для которой нет более точной категории домена.
     *
     * @param cause Исходное исключение, если известна причина сбоя.
     */
    class Unknown(cause: Throwable? = null) : DomainError(cause?.message, cause)
}

/** Сохраняет существующую ошибку домена или оборачивает неклассифицированный сбой. */
fun Throwable.toDomainError(): DomainError = this as? DomainError ?: DomainError.Unknown(this)

/** Возвращает результат доменной операции и передаёт исключение отмены корутины вызывающему коду. */
inline fun <T> domainCatching(block: () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Result.failure(e.toDomainError())
    }
