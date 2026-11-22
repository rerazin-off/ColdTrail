package com.example.mrdetective.domain.models

data class CodeChallenge(
    val id: Int,
    val title: String,
    val hint: String?,
    val rewardClueId: Int,
    val solved: Boolean,
    val rewardClaimed: Boolean,
)

data class CodeChallenges(val investigationId: Int, val puzzles: List<CodeChallenge>)
