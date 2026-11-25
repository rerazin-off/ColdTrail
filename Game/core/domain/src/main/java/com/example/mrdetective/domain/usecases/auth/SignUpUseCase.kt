package com.example.mrdetective.domain.usecases.auth

import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.repositories.AuthRepository
import javax.inject.Inject

/** Validates registration fields before creating a player account. */
class SignUpUseCase
    @Inject
    constructor(
        private val repository: AuthRepository,
    ) {
        /** Checks registration details before calling the auth repository. */
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
