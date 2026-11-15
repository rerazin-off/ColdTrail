package com.example.mrdetective.domain.models

data class Puzzle(
    val id: Int,
    val encodedPattern: String,
    val expectedAnswer: String,
    val clueRewardId: Int?
)