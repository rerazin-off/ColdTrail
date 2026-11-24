package com.example.mrdetective.data.network

import com.example.mrdetective.domain.models.DomainError
import retrofit2.HttpException
import java.io.IOException
import kotlinx.coroutines.CancellationException
import java.net.SocketTimeoutException

object NetworkErrorHandler {
    fun map(throwable: Throwable): DomainError = when (throwable) {
        is CancellationException -> throw throwable
        is NoConnectionException -> DomainError.Network(throwable, DomainError.Network.Kind.Offline)
        is SocketTimeoutException -> DomainError.Network(throwable)
        is IOException -> DomainError.Network(throwable)
        is HttpException -> when (throwable.code()) {
            400, 422 -> DomainError.Validation("Проверьте введённые данные.")
            401, 403 -> DomainError.Unauthorized()
            404 -> DomainError.NotFound("Запрошенные данные не найдены.")
            408 -> DomainError.Network(throwable)
            in 500..599 -> DomainError.Network(throwable, DomainError.Network.Kind.Server)
            429 -> DomainError.Validation("Превышен лимит запросов. Попробуйте позже.")
            else -> DomainError.Unknown(throwable)
        }
        else -> throwable as? DomainError ?: DomainError.Unknown(throwable)
    }
}

