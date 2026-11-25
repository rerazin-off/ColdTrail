package com.example.mrdetective.domain.models

/**
 * Описывает головоломку Морзе и состояние её награды.
 *
 * @property id Идентификатор головоломки Морзе.
 * @property title Название дела, улики или головоломки для отображения.
 * @property hint Подсказка к головоломке, если она задана.
 * @property signal Сигнал Морзе в виде точек, тире и разделителей.
 * @property rewardClueId Идентификатор улики, выдаваемой за головоломку.
 * @property solved Подтверждено ли правильное решение головоломки.
 * @property rewardClaimed Уже ли получена улика за головоломку.
 */
data class MorseChallenge(
    val id: Int,
    val title: String,
    val hint: String?,
    val signal: String,
    val rewardClueId: Int,
    val solved: Boolean,
    val rewardClaimed: Boolean,
) {
    private val letters = signal.split(' ')
    val answerLength: Int get() = letters.size
    val numericOnly: Boolean get() = letters.all { it.length == 5 }
}

/**
 * Группирует головоломки Морзе по расследованию.
 *
 * @property investigationId Идентификатор расследования. Для локально начатого дела до синхронизации он может
 *   быть отрицательным.
 * @property puzzles Доступные головоломки Морзе этого расследования.
 */
data class MorseChallenges(val investigationId: Int, val puzzles: List<MorseChallenge>)
