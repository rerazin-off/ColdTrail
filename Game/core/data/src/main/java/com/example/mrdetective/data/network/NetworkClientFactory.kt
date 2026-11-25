package com.example.mrdetective.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/** Constructs the HTTP transport, logging, and Retrofit client for Supabase. */
object NetworkClientFactory {
    /** Creates the HTTP logger and redacts credentials in headers. */
    fun logging(debug: Boolean): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (debug) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
            redactHeader("Authorization")
            redactHeader("apikey")
            redactHeader("Cookie")
        }

    /** Configures request interceptors, session refresh, and network timeouts. */
    fun okHttp(
        authInterceptor: AuthInterceptor,
        loggingInterceptor: HttpLoggingInterceptor,
        tokenAuthenticator: SupabaseTokenAuthenticator,
        networkAvailabilityInterceptor: NetworkAvailabilityInterceptor,
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(networkAvailabilityInterceptor)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .authenticator(tokenAuthenticator)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            .build()

    /** Validates the base URL configuration and attaches the JSON converter to Retrofit. */
    fun retrofit(client: OkHttpClient): Retrofit {
        SupabaseConfig.validate()
        return Retrofit.Builder()
            .baseUrl(SupabaseConfig.baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
