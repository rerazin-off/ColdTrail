package com.example.mrdetective.data.remote.api

import com.example.mrdetective.data.remote.dto.AuthRequestDto
import com.example.mrdetective.data.remote.dto.AuthResponseDto
import com.example.mrdetective.data.remote.dto.SignUpRequestDto
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface SupabaseAuthApi {
    @POST("auth/v1/token")
    suspend fun signIn(@Query("grant_type") grantType: String = "password", @Body body: AuthRequestDto): AuthResponseDto

    @POST("auth/v1/signup")
    suspend fun signUp(@Body body: SignUpRequestDto): AuthResponseDto

    @POST("auth/v1/logout")
    suspend fun signOut()
}
