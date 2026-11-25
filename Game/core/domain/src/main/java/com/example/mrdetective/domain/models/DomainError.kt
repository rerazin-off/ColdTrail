package com.example.mrdetective.domain.models

import kotlin.coroutines.cancellation.CancellationException

/** Определяет общие категории ошибок для домена, слоя данных и интерфейса. */
sealed class DomainError(
    message: String? = null,
    cause: Throwable? = null,
) : Exception(message, cause) {
    /** Обозначает ошибку ввода или игрового действия, которую может исправить игрок. */
    class Validation(message: String) : DomainError(message)

    /** Обозначает отсутствие запрошенных игровых данных. */
    class NotFound(message: String = "Не найдено") : DomainError(message)

    /** Обозначает отсутствие или истечение авторизованной сессии. */
    class Unauthorized(message: String = "Нет доступа") : DomainError(message)

    /** Представляет сетевой сбой и его категорию. */
    class Network(
        cause: Throwable? = null,
        val kind: Kind = Kind.Connection,
    ) : DomainError("Ошибка сети", cause) {
        /** Различает отсутствие сети, сбой сервера и другие сбои соединения. */
        enum class Kind { Offline, Server, Connection }
    }

    /** Обозначает операцию, ожидающую синхронизации. */
    class SyncPending : DomainError("Данные ожидают синхронизации")

    /** Обозначает недоступность нужного оборудования устройства. */
    class HardwareUnavailable : DomainError("Вибрация недоступна на этом устройстве")

    /** Сохраняет причину ошибки, для которой нет более точной категории домена. */
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
