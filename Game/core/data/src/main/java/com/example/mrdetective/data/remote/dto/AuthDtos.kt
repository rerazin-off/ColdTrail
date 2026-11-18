package com.example.mrdetective.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AuthRequestDto(val email: String, val password: String)
data class SignUpRequestDto(val email: String, val password: String, @SerializedName("data") val metadata: Map<String, String> = emptyMap())
data class AuthUserDto(val id: String, val email: String? = null)
data class AuthResponseDto(
    @SerializedName("access_token") val accessToken: String? = null,
    @SerializedName("refresh_token") val refreshToken: String? = null,
    @SerializedName("user") val user: AuthUserDto? = null,
    val id: String? = null,
)
