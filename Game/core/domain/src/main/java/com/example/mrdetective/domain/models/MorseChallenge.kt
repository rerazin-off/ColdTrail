package com.example.mrdetective.domain.models

/** Описывает головоломку Морзе и состояние её награды. */
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

/** Группирует головоломки Морзе по расследованию. */
data class MorseChallenges(val investigationId: Int, val puzzles: List<MorseChallenge>)
