package com.example.mrdetective.data.network

import com.example.mrdetective.domain.models.DomainError
import retrofit2.HttpException
import java.io.IOException
import kotlinx.coroutines.CancellationException
import java.net.SocketTimeoutException

object NetworkErrorHandler {
    fun map(throwable: Throwable): DomainError = when (throwable) {
        is CancellationException -> throw throwable
        is SocketTimeoutException -> DomainError.Network(throwable)
        is IOException -> DomainError.Network(throwable)
        is HttpException -> when (throwable.code()) {
            400, 422 -> DomainError.Validation(errorMessage(throwable) ?: "Некорректный запрос")
            401, 403 -> DomainError.Unauthorized()
            404 -> DomainError.NotFound(errorMessage(throwable) ?: "Ресурс не найден")
            408, in 500..599 -> DomainError.Network(throwable)
            429 -> DomainError.Validation("Превышен лимит запросов. Попробуйте позже.")
            else -> DomainError.Unknown(throwable)
        }
        else -> throwable as? DomainError ?: DomainError.Unknown(throwable)
    }

    private fun errorMessage(error: HttpException): String? = try {
        error.response()?.errorBody()?.string()
            ?.takeIf(String::isNotBlank)
            ?.take(500)
    } catch (_: Exception) {
        null
    }
}

