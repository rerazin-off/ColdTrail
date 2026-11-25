package com.example.mrdetective.domain.repositories

/** Определяет операции регистрации, входа и управления сессией. */
interface AuthRepository {
    /** Открывает авторизованную сессию по переданным учётным данным. */
    suspend fun signIn(
        email: String,
        password: String,
    ): Result<String>

    /** Регистрирует аккаунт по переданным учётным данным. */
    suspend fun signUp(
        email: String,
        password: String,
    ): Result<String>

    /** Завершает текущую сессию игрока. */
    suspend fun signOut()

    /** Возвращает идентификатор активного аккаунта, если он доступен. */
    suspend fun currentUser(): String?
}
