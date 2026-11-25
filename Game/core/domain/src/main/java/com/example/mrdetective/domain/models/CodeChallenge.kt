package com.example.mrdetective.domain.models

/** Describes a numeric puzzle and whether its reward has been claimed. */
data class CodeChallenge(
    val id: Int,
    val title: String,
    val hint: String?,
    val rewardClueId: Int,
    val solved: Boolean,
    val rewardClaimed: Boolean,
)

/** Groups numeric puzzles by investigation. */
data class CodeChallenges(val investigationId: Int, val puzzles: List<CodeChallenge>)
