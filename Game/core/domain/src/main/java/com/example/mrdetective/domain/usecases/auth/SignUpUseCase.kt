package com.example.mrdetective.domain.usecases.auth

import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.repositories.AuthRepository
import javax.inject.Inject

class SignUpUseCase
    @Inject
    constructor(
        private val repository: AuthRepository,
    ) {
        suspend operator fun invoke(
            email: String,
            password: String,
        ): Result<String> {
            if (email.isBlank() || password.isBlank()) {
                return Result.failure(
                    DomainError.Validation("Заполните все поля"),
                )
            }
            if (password.length < 6) {
                return Result.failure(
                    DomainError.Validation("Пароль слишком короткий"),
                )
            }

            return repository.signUp(email, password)
        }
    }
