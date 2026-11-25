package com.example.mrdetective.domain.usecases.auth

import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.repositories.AuthRepository
import javax.inject.Inject

/**
 * Проверяет учётные данные перед запуском сессии игрока.
 *
 * @param repository Репозиторий для обращения к сервису авторизации.
 */
class SignInUseCase
    @Inject
    constructor(
        private val repository: AuthRepository,
    ) {
        /**
         * Проверяет формат почты и пароля перед вызовом репозитория авторизации.
         *
         * @param email Электронная почта пользователя.
         * @param password Пароль пользователя. Передаётся без изменений.
         * @return Идентификатор вошедшего пользователя или ошибка входа.
         */
        suspend operator fun invoke(
            email: String,
            password: String,
        ): Result<String> {
            validateAuthCredentials(email, password, register = false)?.let {
                return Result.failure(DomainError.Validation(it))
            }

            return repository.signIn(email.trim(), password)
        }
    }
