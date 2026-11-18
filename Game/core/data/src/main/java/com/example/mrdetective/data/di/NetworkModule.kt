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


@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides @Singleton
    fun provideGson(): Gson = Gson()

    @Provides @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        NetworkClientFactory.logging(BuildConfig.DEBUG)

    @Provides @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        loggingInterceptor: HttpLoggingInterceptor,
        tokenAuthenticator: SupabaseTokenAuthenticator,
        networkAvailabilityInterceptor: NetworkAvailabilityInterceptor,
    ): OkHttpClient = NetworkClientFactory.okHttp(
        authInterceptor, loggingInterceptor, tokenAuthenticator, networkAvailabilityInterceptor,
    )

    @Provides @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit = NetworkClientFactory.retrofit(client)

    @Provides @Singleton
    fun provideSupabaseRestApi(retrofit: Retrofit): SupabaseRestApi =
        retrofit.create(SupabaseRestApi::class.java)

    @Provides @Singleton
    fun provideSupabaseAuthApi(retrofit: Retrofit): SupabaseAuthApi =
        retrofit.create(SupabaseAuthApi::class.java)
    @Provides @Singleton
    fun provideOfflineApi(retrofit: Retrofit): OfflineApi = retrofit.create(OfflineApi::class.java)
}
