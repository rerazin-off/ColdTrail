package com.example.mrdetective.domain.models

/**
 * Хранит заметку игрока по расследованию и её временные метки.
 *
 * @property id Идентификатор заметки.
 * @property investigationId Идентификатор расследования. Для локально начатого дела до синхронизации он может
 *   быть отрицательным.
 * @property text Текст заметки игрока.
 * @property createdAt Время создания записи.
 * @property updatedAt Время последнего обновления записи.
 */
data class Note(
    val id: Int,
    val investigationId: Int,
    val text: String,
    val createdAt: String,
    val updatedAt: String,
)
