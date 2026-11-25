package com.example.mrdetective.data.network

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.annotation.RequiresPermission
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Reports whether Android has validated internet access on the active network. */
@Singleton
class ConnectivityChecker
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        /** Checks for an active Android network with validated internet access. */
        @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
        fun isNetworkAvailable(): Boolean {
            val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = manager.activeNetwork ?: return false
            val capabilities = manager.getNetworkCapabilities(network) ?: return false
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        }
    }
