package com.example.mrdetective.data.network

import com.example.mrdetective.domain.models.DomainError
import kotlinx.coroutines.CancellationException
import java.io.IOException

suspend inline fun <T> apiCall(crossinline block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: Throwable) {
    Result.failure(NetworkErrorHandler.map(e))
}
suspend fun <T : Any> remoteOrCache(
    connectivity: ConnectivityChecker,
    cached: suspend () -> T?,
    remote: suspend () -> T,
): Result<T> {
    return try {
        if (!connectivity.isNetworkAvailable()) {
            val localValue = cached()
            if (localValue != null) {
                Result.success(localValue)
            } else {
                Result.failure(
                    NetworkErrorHandler.map(IOException("Нет подключения к интернету и локального кеша нет"))
                )
            }
        } else {
            try {
                Result.success(remote())
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (remoteError: Throwable) {
                val localValue = cached()
                if (localValue != null) Result.success(localValue)
                else Result.failure(NetworkErrorHandler.map(remoteError))
            }
        }
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (error: Throwable) {
        Result.failure(NetworkErrorHandler.map(error))
    }
}

