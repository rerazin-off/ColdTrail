package com.example.mrdetective.data.remote.api

import com.example.mrdetective.data.remote.dto.AuthRequestDto
import com.example.mrdetective.data.remote.dto.AuthResponseDto
import com.example.mrdetective.data.remote.dto.SignUpRequestDto
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

/** Retrofit contract for Supabase authentication endpoints. */
interface SupabaseAuthApi {
    /** Exchanges email and password for a Supabase session. */
    @POST("auth/v1/token")
    suspend fun signIn(
        @Query("grant_type") grantType: String = "password",
        @Body body: AuthRequestDto,
    ): AuthResponseDto

    /** Registers an account with Supabase Auth. */
    @POST("auth/v1/signup")
    suspend fun signUp(
        @Body body: SignUpRequestDto,
    ): AuthResponseDto

    /** Invalidates the current session through Supabase Auth. */
    @POST("auth/v1/logout")
    suspend fun signOut()
}
