package com.example.mrdetective.data.network

import android.Manifest
import androidx.annotation.RequiresPermission
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/** Fails a request early when the device lacks a network connection. */
@Singleton
class NetworkAvailabilityInterceptor
    @Inject
    constructor(
        private val connectivityChecker: ConnectivityChecker,
    ) : Interceptor {
        /** Rejects calls when offline before handing them to the network client. */
        @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
        override fun intercept(chain: Interceptor.Chain): Response {
            if (!connectivityChecker.isNetworkAvailable()) {
                throw NoConnectionException()
            }
            return chain.proceed(chain.request())
        }
    }
