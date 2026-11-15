package com.example.mrdetective.domain.models

data class CollectedClue(
    val investigationId: Int,
    val clueId: Int,
    val collectedAt: String
)