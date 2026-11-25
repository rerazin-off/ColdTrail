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

/** Предоставляет общий HTTP-клиент Supabase и интерфейсы Retrofit. */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    /** Предоставляет общий сериализатор JSON для слоя данных. */
    @Provides @Singleton
    fun provideGson(): Gson = Gson()

    /** Предоставляет журналирование, настроенное для текущего типа сборки. */
    @Provides @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor = NetworkClientFactory.logging(BuildConfig.DEBUG)

    /** Создаёт общий HTTP-клиент с поддержкой авторизации. */
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

    /** Создаёт экземпляр Retrofit с базовым URL Supabase. */
    @Provides @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit = NetworkClientFactory.retrofit(client)

    /** Создаёт реализацию Retrofit для REST API таблиц. */
    @Provides @Singleton
    fun provideSupabaseRestApi(retrofit: Retrofit): SupabaseRestApi = retrofit.create(SupabaseRestApi::class.java)

    /** Создаёт реализацию Retrofit для API авторизации. */
    @Provides @Singleton
    fun provideSupabaseAuthApi(retrofit: Retrofit): SupabaseAuthApi = retrofit.create(SupabaseAuthApi::class.java)

    /** Создаёт реализацию Retrofit для API расследований и синхронизации. */
    @Provides @Singleton
    fun provideOfflineApi(retrofit: Retrofit): OfflineApi = retrofit.create(OfflineApi::class.java)
}
