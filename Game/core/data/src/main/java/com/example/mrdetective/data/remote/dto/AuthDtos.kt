package com.example.mrdetective.data.remote.dto

import com.google.gson.annotations.SerializedName

/** Credentials sent to the password sign-in endpoint. */
data class AuthRequestDto(val email: String, val password: String)

/** Account details sent to the registration endpoint. */
data class SignUpRequestDto(
    val email: String,
    val password: String,
    @SerializedName("data") val metadata: Map<String, String> = emptyMap(),
)

/** User information returned by Supabase Auth. */
data class AuthUserDto(val id: String, val email: String? = null)

/** Authentication response containing the user and any issued session tokens. */
data class AuthResponseDto(
    @SerializedName("access_token") val accessToken: String? = null,
    @SerializedName("refresh_token") val refreshToken: String? = null,
    @SerializedName("user") val user: AuthUserDto? = null,
    val id: String? = null,
)
