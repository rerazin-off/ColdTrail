package com.example.mrdetective.domain.usecases.auth

private val EmailPattern = Regex("[^@\\s]+@[^@\\s.]+(?:\\.[^@\\s.]+)+")

fun validateAuthCredentials(email: String, password: String, register: Boolean): String? = when {
    !EmailPattern.matches(email.trim()) -> "Введите корректный адрес почты."
    password.isBlank() -> "Введите пароль."
    register && password.length < 6 -> "Пароль должен содержать не менее 6 символов."
    else -> null
}
