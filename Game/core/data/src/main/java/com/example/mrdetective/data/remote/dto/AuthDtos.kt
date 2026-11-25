package com.example.mrdetective.data.remote.dto

import com.google.gson.annotations.SerializedName

/** Учётные данные для входа по паролю. */
data class AuthRequestDto(val email: String, val password: String)

/** Данные аккаунта, отправляемые при регистрации. */
data class SignUpRequestDto(
    val email: String,
    val password: String,
    @SerializedName("data") val metadata: Map<String, String> = emptyMap(),
)

/** Данные пользователя, возвращаемые Supabase Auth. */
data class AuthUserDto(val id: String, val email: String? = null)

/** Ответ авторизации с данными пользователя и выданными токенами сессии. */
data class AuthResponseDto(
    @SerializedName("access_token") val accessToken: String? = null,
    @SerializedName("refresh_token") val refreshToken: String? = null,
    @SerializedName("user") val user: AuthUserDto? = null,
    val id: String? = null,
)
