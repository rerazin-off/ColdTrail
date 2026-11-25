package com.example.mrdetective.domain.models

/**
 * Связывает улику с расследованием, в котором её нашли.
 *
 * @property investigationId Идентификатор расследования. Для локально начатого дела до синхронизации он может
 *   быть отрицательным.
 * @property clueId Идентификатор улики.
 * @property collectedAt Время получения улики.
 */
data class CollectedClue(
    val investigationId: Int,
    val clueId: Int,
    val collectedAt: String,
)
