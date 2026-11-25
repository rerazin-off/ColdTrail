package com.example.mrdetective.domain.models

/** Represents a clue puzzle and its optional reward. */
data class Puzzle(
    val id: Int,
    val encodedPattern: String,
    val expectedAnswer: String,
    val clueRewardId: Int?,
)
