package com.example.mrdetective.domain.usecases.auth

import com.example.mrdetective.domain.repositories.AuthRepository
import com.example.mrdetective.domain.models.DomainError
import javax.inject.Inject

class SignInUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String
    ): Result<String> {
        if (email.isBlank()) {
            return Result.failure(
                DomainError.Validation("Email не может быть пустым")
            )
        }

        if (password.isBlank()) {
            return Result.failure(
                DomainError.Validation("Пароль не может быть пустым")
            )
        }

        return repository.signIn(email, password)
    }
}