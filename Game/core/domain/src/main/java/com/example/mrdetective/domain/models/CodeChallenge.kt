package com.example.mrdetective.domain.models

/** Описывает числовую головоломку и состояние получения награды. */
data class CodeChallenge(
    val id: Int,
    val title: String,
    val hint: String?,
    val rewardClueId: Int,
    val solved: Boolean,
    val rewardClaimed: Boolean,
)

/** Группирует числовые головоломки по расследованию. */
data class CodeChallenges(val investigationId: Int, val puzzles: List<CodeChallenge>)
