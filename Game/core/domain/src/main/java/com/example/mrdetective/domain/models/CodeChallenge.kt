package com.example.mrdetective.domain.models

/**
 * Описывает числовую головоломку и состояние получения награды.
 *
 * @property id Идентификатор числовой головоломки.
 * @property title Название дела, улики или головоломки для отображения.
 * @property hint Подсказка к головоломке, если она задана.
 * @property rewardClueId Идентификатор улики, выдаваемой за головоломку.
 * @property solved Подтверждено ли правильное решение головоломки.
 * @property rewardClaimed Уже ли получена улика за головоломку.
 */
data class CodeChallenge(
    val id: Int,
    val title: String,
    val hint: String?,
    val rewardClueId: Int,
    val solved: Boolean,
    val rewardClaimed: Boolean,
)

/**
 * Группирует числовые головоломки по расследованию.
 *
 * @property investigationId Идентификатор расследования. Для локально начатого дела до синхронизации он может
 *   быть отрицательным.
 * @property puzzles Доступные числовые головоломки этого расследования.
 */
data class CodeChallenges(val investigationId: Int, val puzzles: List<CodeChallenge>)
