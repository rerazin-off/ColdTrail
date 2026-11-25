package com.example.mrdetective.domain.models

/** Представляет головоломку и её необязательную награду. */
data class Puzzle(
    val id: Int,
    val encodedPattern: String,
    val expectedAnswer: String,
    val clueRewardId: Int?,
)
