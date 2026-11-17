package com.example.mrdetective.data.repository

import com.example.mrdetective.data.local.datastore.SettingsData
import com.example.mrdetective.data.network.SessionTokenStore
import com.example.mrdetective.data.network.apiCall
import com.example.mrdetective.data.remote.api.SupabaseAuthApi
import com.example.mrdetective.data.remote.dto.AuthRequestDto
import com.example.mrdetective.data.remote.dto.SignUpRequestDto
import com.example.mrdetective.domain.repositories.AuthRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseAuthRepositoryImpl @Inject constructor(
    private val api: SupabaseAuthApi,
    private val tokenStore: SessionTokenStore,
    private val settings: SettingsData,
) : AuthRepository {
    override suspend fun signIn(email: String, password: String): Result<String> = apiCall {
        val response = api.signIn(body = AuthRequestDto(email, password))
        tokenStore.setSession(response.accessToken, response.refreshToken)
        settings.setUserId(response.user.id)
        response.user.id
    }

    override suspend fun signUp(email: String, password: String): Result<String> = apiCall {
        val response = api.signUp(SignUpRequestDto(email, password))
        tokenStore.setSession(response.accessToken, response.refreshToken)
        settings.setUserId(response.user.id)
        response.user.id
    }

    override suspend fun signOut() {
        try { api.signOut() } finally { tokenStore.clear(); settings.setUserId(null) }
    }

    override suspend fun currentUser(): String? = settings.userId.first()
}

