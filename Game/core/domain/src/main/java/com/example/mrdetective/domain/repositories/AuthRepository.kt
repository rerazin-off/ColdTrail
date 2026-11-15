package com.example.mrdetective.domain.repositories

interface AuthRepository {
    suspend fun signIn(
        email: String,
        password: String,
    ): Result<String>

    suspend fun signUp(
        email: String,
        password: String,
    ): Result<String>

    suspend fun signOut()

    suspend fun currentUser(): String?
}
