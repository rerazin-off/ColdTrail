package com.example.mrdetective.data.repository

import android.content.Context
import com.example.mrdetective.data.local.datastore.SettingsData
import com.example.mrdetective.data.network.SessionTokenStore
import com.example.mrdetective.data.network.apiCall
import com.example.mrdetective.data.remote.api.SupabaseAuthApi
import com.example.mrdetective.data.remote.dto.AuthRequestDto
import com.example.mrdetective.data.remote.dto.SignUpRequestDto
import com.example.mrdetective.data.sync.SyncScheduler
import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.repositories.AuthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

/**
 * Работает с Supabase Auth и хранит сессию игрока на устройстве.
 *
 * @param api Retrofit API входа, регистрации и выхода из сессии.
 * @param tokenStore Локальное хранилище токенов авторизации.
 * @param settings Хранилище настроек приложения и данных профиля в DataStore.
 * @param context Контекст Android для доступа к ресурсам и системным службам.
 */
@Singleton
class AuthRepositoryImpl
    @Inject
    constructor(
        private val api: SupabaseAuthApi,
        private val tokenStore: SessionTokenStore,
        private val settings: SettingsData,
        @param:ApplicationContext private val context: Context,
    ) : AuthRepository {
        /** Открывает авторизованную сессию по переданным учётным данным. */
        override suspend fun signIn(
            email: String,
            password: String,
        ): Result<String> =
            apiCall {
                val response = api.signIn(body = AuthRequestDto(email, password))
                val userId = response.user?.id?.takeIf(String::isNotBlank) ?: throw DomainError.Unauthorized()
                val accessToken = response.accessToken?.takeIf(String::isNotBlank) ?: throw DomainError.Unauthorized()
                settings.setUserId(userId)
                tokenStore.setSession(accessToken, response.refreshToken)
                SyncScheduler.enqueue(context)
                userId
            }

        /** Регистрирует аккаунт по переданным учётным данным. */
        override suspend fun signUp(
            email: String,
            password: String,
        ): Result<String> =
            apiCall {
                val response = api.signUp(SignUpRequestDto(email, password))
                val userId =
                    (response.user?.id ?: response.id)?.takeIf(String::isNotBlank)
                        ?: throw DomainError.Validation("Не удалось создать аккаунт.")
                response.accessToken?.takeIf(String::isNotBlank)?.let {
                    settings.setUserId(userId)
                    tokenStore.setSession(it, response.refreshToken)
                    SyncScheduler.enqueue(context)
                }
                userId
            }

        /** Завершает текущую сессию игрока. */
        override suspend fun signOut() {
            try {
                api.signOut()
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                // Локальный выход из аккаунта работает без сети и после истечения сессии.
            }
            settings.setUserId(null)
            tokenStore.clear()
        }

        /** Возвращает идентификатор активного аккаунта, если он доступен. */
        override suspend fun currentUser(): String? = if (tokenStore.session() == null) null else settings.userId.first()
    }
