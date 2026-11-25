package com.example.mrdetective.domain.usecases.auth

import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.repositories.AuthRepository
import javax.inject.Inject

/** Validates credentials before starting a player session. */
class SignInUseCase
    @Inject
    constructor(
        private val repository: AuthRepository,
    ) {
        /** Checks the email and password format before calling the auth repository. */
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
