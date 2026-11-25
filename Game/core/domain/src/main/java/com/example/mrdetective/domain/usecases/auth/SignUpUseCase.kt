package com.example.mrdetective.domain.usecases.auth

import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.repositories.AuthRepository
import javax.inject.Inject

/**
 * Проверяет поля регистрации перед созданием аккаунта.
 *
 * @param repository Репозиторий для обращения к сервису авторизации.
 */
class SignUpUseCase
    @Inject
    constructor(
        private val repository: AuthRepository,
    ) {
        /**
         * Проверяет данные регистрации перед вызовом репозитория авторизации.
         *
         * @param email Электронная почта пользователя.
         * @param password Пароль пользователя. Передаётся без изменений.
         * @return Идентификатор зарегистрированного пользователя или ошибка регистрации.
         */
        suspend operator fun invoke(
            email: String,
            password: String,
        ): Result<String> {
            validateAuthCredentials(email, password, register = true)?.let {
                return Result.failure(DomainError.Validation(it))
            }

            return repository.signUp(email.trim(), password)
        }
    }
