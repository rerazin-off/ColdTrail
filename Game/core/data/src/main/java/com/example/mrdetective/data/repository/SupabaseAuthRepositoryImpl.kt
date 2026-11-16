package com.example.mrdetective.data.repository

import com.example.mrdetective.data.network.SessionTokenStore
import com.example.mrdetective.data.network.apiCall
import com.example.mrdetective.data.remote.api.SupabaseAuthApi
import com.example.mrdetective.data.remote.dto.AuthRequestDto
import com.example.mrdetective.data.remote.dto.SignUpRequestDto
import com.example.mrdetective.domain.repositories.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseAuthRepositoryImpl @Inject constructor(
    private val api: SupabaseAuthApi,
    private val tokenStore: SessionTokenStore,
) : AuthRepository {
    @Volatile private var userId: String? = null

    override suspend fun signIn(email: String, password: String): Result<String> = apiCall {
        val response = api.signIn(body = AuthRequestDto(email, password))
        tokenStore.setSession(response.accessToken, response.refreshToken)
        userId = response.user.id
        response.user.id
    }

    override suspend fun signUp(email: String, password: String): Result<String> = apiCall {
        val response = api.signUp(SignUpRequestDto(email, password))
        tokenStore.setSession(response.accessToken, response.refreshToken)
        userId = response.user.id
        response.user.id
    }

    override suspend fun signOut() {
        try { api.signOut() } finally { tokenStore.clear(); userId = null }
    }

    override suspend fun currentUser(): String? = userId
}
