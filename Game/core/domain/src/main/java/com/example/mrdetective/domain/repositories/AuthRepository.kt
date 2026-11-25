package com.example.mrdetective.domain.repositories

/** Defines the operations for registration, sign-in, and session management. */
interface AuthRepository {
    /** Starts an authenticated session using the provided credentials. */
    suspend fun signIn(
        email: String,
        password: String,
    ): Result<String>

    /** Registers a new account using the provided credentials. */
    suspend fun signUp(
        email: String,
        password: String,
    ): Result<String>

    /** Ends the current player's session. */
    suspend fun signOut()

    /** Returns the active account identifier, when available. */
    suspend fun currentUser(): String?
}
