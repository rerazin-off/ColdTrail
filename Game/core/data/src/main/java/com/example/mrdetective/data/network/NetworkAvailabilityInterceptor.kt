package com.example.mrdetective.data.network

import android.Manifest
import androidx.annotation.RequiresPermission
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Отклоняет запрос, если у устройства нет сетевого подключения.
 *
 * @param connectivityChecker Проверка доступности сети перед сетевым запросом.
 */
@Singleton
class NetworkAvailabilityInterceptor
    @Inject
    constructor(
        private val connectivityChecker: ConnectivityChecker,
    ) : Interceptor {
        /** Отклоняет запросы без сети до обращения к HTTP-клиенту. */
        @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
        override fun intercept(chain: Interceptor.Chain): Response {
            if (!connectivityChecker.isNetworkAvailable()) {
                throw NoConnectionException()
            }
            return chain.proceed(chain.request())
        }
    }
