package com.example.mrdetective.domain.models

import kotlin.coroutines.cancellation.CancellationException

sealed class DomainError(
    message: String? = null,
    cause: Throwable? = null
) : Exception(message, cause) {

    class Validation(message: String) : DomainError(message)
    class NotFound(message: String = "Не найдено") : DomainError(message)
    class Unauthorized(message: String = "Нет доступа") : DomainError(message)
    class Network(cause: Throwable? = null) : DomainError("Ошибка сети", cause)
    class Unknown(cause: Throwable? = null) : DomainError(cause?.message, cause)
}

fun Throwable.toDomainError(): DomainError =
    this as? DomainError ?: DomainError.Unknown(this)
inline fun <T> domainCatching(block: () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Result.failure(e.toDomainError())
    }
