package com.example.mrdetective.domain.models

/**
 * Представляет головоломку и её необязательную награду.
 *
 * @property id Идентификатор головоломки.
 * @property encodedPattern Закодированное условие головоломки.
 * @property expectedAnswer Ожидаемый ответ для локальных сценариев. Для серверных головоломок может быть
 *   пустым.
 * @property clueRewardId Идентификатор улики-награды, если она задана.
 */
data class Puzzle(
    val id: Int,
    val encodedPattern: String,
    val expectedAnswer: String,
    val clueRewardId: Int?,
)
