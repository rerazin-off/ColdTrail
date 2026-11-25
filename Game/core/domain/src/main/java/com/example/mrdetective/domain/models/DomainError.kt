package com.example.mrdetective.domain.models

import kotlin.coroutines.cancellation.CancellationException

/** Classifies failures shared by the domain, data, and presentation layers. */
sealed class DomainError(
    message: String? = null,
    cause: Throwable? = null,
) : Exception(message, cause) {
    /** Indicates an input or game action that the player must correct. */
    class Validation(message: String) : DomainError(message)

    /** Indicates that the requested game data could not be found. */
    class NotFound(message: String = "Не найдено") : DomainError(message)

    /** Indicates that an authenticated session is required or no longer valid. */
    class Unauthorized(message: String = "Нет доступа") : DomainError(message)

    /** Represents a network-related failure and its category. */
    class Network(
        cause: Throwable? = null,
        val kind: Kind = Kind.Connection,
    ) : DomainError("Ошибка сети", cause) {
        /** Distinguishes offline, server, and other network failures. */
        enum class Kind { Offline, Server, Connection }
    }

    /** Indicates that an operation awaits synchronization. */
    class SyncPending : DomainError("Данные ожидают синхронизации")

    /** Indicates that the device cannot use the requested hardware. */
    class HardwareUnavailable : DomainError("Вибрация недоступна на этом устройстве")

    /** Preserves the cause of a failure without a more specific domain category. */
    class Unknown(cause: Throwable? = null) : DomainError(cause?.message, cause)
}

/** Preserves an existing domain error or wraps an unclassified failure. */
fun Throwable.toDomainError(): DomainError = this as? DomainError ?: DomainError.Unknown(this)

/** Captures a domain operation as a result while allowing coroutine cancellation to propagate. */
inline fun <T> domainCatching(block: () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Result.failure(e.toDomainError())
    }
