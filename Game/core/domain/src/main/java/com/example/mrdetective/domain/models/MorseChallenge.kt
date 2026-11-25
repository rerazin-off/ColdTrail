package com.example.mrdetective.domain.models

/** Describes a Morse puzzle and the current reward state. */
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

/** Groups Morse puzzles by investigation. */
data class MorseChallenges(val investigationId: Int, val puzzles: List<MorseChallenge>)
