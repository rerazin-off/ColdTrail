package com.example.mrdetective.data.di

import com.example.mrdetective.data.BuildConfig
import com.example.mrdetective.data.network.AuthInterceptor
import com.example.mrdetective.data.network.NetworkAvailabilityInterceptor
import com.example.mrdetective.data.network.NetworkClientFactory
import com.example.mrdetective.data.network.SupabaseTokenAuthenticator
import com.example.mrdetective.data.remote.api.OfflineApi
import com.example.mrdetective.data.remote.api.SupabaseAuthApi
import com.example.mrdetective.data.remote.api.SupabaseRestApi
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

/** Provides the shared Supabase HTTP client and Retrofit APIs. */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    /** Provides the JSON serializer shared by the data layer. */
    @Provides @Singleton
    fun provideGson(): Gson = Gson()

    /** Provides logging configured for the current build type. */
    @Provides @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor = NetworkClientFactory.logging(BuildConfig.DEBUG)

    /** Creates the shared authenticated HTTP client. */
    @Provides @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        loggingInterceptor: HttpLoggingInterceptor,
        tokenAuthenticator: SupabaseTokenAuthenticator,
        networkAvailabilityInterceptor: NetworkAvailabilityInterceptor,
    ): OkHttpClient =
        NetworkClientFactory.okHttp(
            authInterceptor,
            loggingInterceptor,
            tokenAuthenticator,
            networkAvailabilityInterceptor,
        )

    /** Creates the Retrofit instance for the Supabase base URL. */
    @Provides @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit = NetworkClientFactory.retrofit(client)

    /** Creates the Retrofit implementation of the table REST API. */
    @Provides @Singleton
    fun provideSupabaseRestApi(retrofit: Retrofit): SupabaseRestApi = retrofit.create(SupabaseRestApi::class.java)

    /** Creates the Retrofit implementation of the authentication API. */
    @Provides @Singleton
    fun provideSupabaseAuthApi(retrofit: Retrofit): SupabaseAuthApi = retrofit.create(SupabaseAuthApi::class.java)

    /** Creates the Retrofit implementation of the investigation and synchronization API. */
    @Provides @Singleton
    fun provideOfflineApi(retrofit: Retrofit): OfflineApi = retrofit.create(OfflineApi::class.java)
}
