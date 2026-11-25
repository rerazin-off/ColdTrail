package com.example.mrdetective.data.remote.api

import com.example.mrdetective.data.remote.dto.AuthRequestDto
import com.example.mrdetective.data.remote.dto.AuthResponseDto
import com.example.mrdetective.data.remote.dto.SignUpRequestDto
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

/** Интерфейс Retrofit для запросов авторизации Supabase. */
interface SupabaseAuthApi {
    /** Обменивает электронную почту и пароль на сессию Supabase. */
    @POST("auth/v1/token")
    suspend fun signIn(
        @Query("grant_type") grantType: String = "password",
        @Body body: AuthRequestDto,
    ): AuthResponseDto

    /** Регистрирует аккаунт через Supabase Auth. */
    @POST("auth/v1/signup")
    suspend fun signUp(
        @Body body: SignUpRequestDto,
    ): AuthResponseDto

    /** Завершает текущую сессию через Supabase Auth. */
    @POST("auth/v1/logout")
    suspend fun signOut()
}
