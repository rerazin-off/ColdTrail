package com.example.mrdetective.domain.usecases.auth

import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.repositories.AuthRepository
import javax.inject.Inject

class SignInUseCase
    @Inject
    constructor(
        private val repository: AuthRepository,
    ) {
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
