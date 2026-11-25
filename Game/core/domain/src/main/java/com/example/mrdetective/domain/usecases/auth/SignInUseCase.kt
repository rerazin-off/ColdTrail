package com.example.mrdetective.domain.usecases.auth

import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.repositories.AuthRepository
import javax.inject.Inject

/** Проверяет учётные данные перед запуском сессии игрока. */
class SignInUseCase
    @Inject
    constructor(
        private val repository: AuthRepository,
    ) {
        /** Проверяет формат почты и пароля перед вызовом репозитория авторизации. */
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
