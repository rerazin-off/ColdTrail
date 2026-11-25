package com.example.mrdetective.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Учётные данные для входа по паролю.
 *
 * @property email Адрес электронной почты игрока.
 * @property password Пароль, введённый игроком.
 */
data class AuthRequestDto(val email: String, val password: String)

/**
 * Данные аккаунта, отправляемые при регистрации.
 *
 * @property email Адрес электронной почты игрока.
 * @property password Пароль, введённый игроком.
 * @property metadata Дополнительные данные при регистрации, например никнейм.
 */
data class SignUpRequestDto(
    val email: String,
    val password: String,
    @SerializedName("data") val metadata: Map<String, String> = emptyMap(),
)

/**
 * Данные пользователя, возвращаемые Supabase Auth.
 *
 * @property id Идентификатор пользователя.
 * @property email Адрес электронной почты игрока.
 */
data class AuthUserDto(val id: String, val email: String? = null)

/**
 * Ответ авторизации с данными пользователя и выданными токенами сессии.
 *
 * @property accessToken Токен доступа из ответа Supabase, если сессия уже создана.
 * @property refreshToken Токен, которым обновляется сессия без повторного ввода пароля.
 * @property user Данные пользователя из ответа Supabase Auth, если они есть.
 * @property id Идентификатор пользователя, если он передан в ответе отдельно.
 */
data class AuthResponseDto(
    @SerializedName("access_token") val accessToken: String? = null,
    @SerializedName("refresh_token") val refreshToken: String? = null,
    @SerializedName("user") val user: AuthUserDto? = null,
    val id: String? = null,
)
