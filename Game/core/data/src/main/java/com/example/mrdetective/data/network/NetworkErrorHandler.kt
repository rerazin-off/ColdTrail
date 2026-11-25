package com.example.mrdetective.data.network

import com.example.mrdetective.domain.models.DomainError
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * Преобразует исключения Retrofit и сетевого слоя в [DomainError].
 *
 * Различает недоступность сети, таймаут, ошибку сервера и прочие ошибки соединения.
 * Это позволяет интерфейсу объяснить причину сбоя и предложить повтор операции.
 */
object NetworkErrorHandler {
    /**
     * Преобразует HTTP- и сетевые исключения в соответствующие ошибки домена.
     *
     * @param throwable Исключение, возникшее при выполнении запроса.
     * @return Исходная или сопоставленная ошибка домена, пригодная для отображения.
     * @throws CancellationException Если корутина запроса была отменена.
     */
    fun map(throwable: Throwable): DomainError =
        when (throwable) {
            is CancellationException -> throw throwable
            is NoConnectionException -> DomainError.Network(throwable, DomainError.Network.Kind.Offline)
            is SocketTimeoutException -> DomainError.Network(throwable, DomainError.Network.Kind.Timeout)
            is IOException -> DomainError.Network(throwable)
            is HttpException ->
                when (throwable.code()) {
                    400, 422 -> DomainError.Validation("Проверьте введённые данные.")
                    401, 403 -> DomainError.Unauthorized()
                    404 -> DomainError.NotFound("Запрошенные данные не найдены.")
                    408, 504 -> DomainError.Network(throwable, DomainError.Network.Kind.Timeout)
                    in 500..599 -> DomainError.Network(throwable, DomainError.Network.Kind.Server)
                    429 -> DomainError.Validation("Превышен лимит запросов. Попробуйте позже.")
                    else -> DomainError.Unknown(throwable)
                }
            else -> throwable as? DomainError ?: DomainError.Unknown(throwable)
        }
}
