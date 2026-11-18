package com.example.mrdetective.data.network


import okhttp3.Interceptor
import okhttp3.Response

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenStore: SessionTokenStore,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("apikey", SupabaseConfig.anonKey)
            .header("Accept", "application/json")
            .apply {
                val path = chain.request().url.encodedPath
                val isPublicAuthRequest = path.endsWith("/auth/v1/token") || path.endsWith("/auth/v1/signup")
                if (!isPublicAuthRequest) {
                    tokenStore.session()?.let {
                        tag(SessionTokenStore.Session::class.java, it)
                        header("Authorization", "Bearer ${it.access}")
                    }
                }
            }
            .build()
        return chain.proceed(request)
    }
}
