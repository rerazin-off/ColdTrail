package com.example.mrdetective.domain.models

/** Associates a clue with the investigation that earned it. */
data class CollectedClue(
    val investigationId: Int,
    val clueId: Int,
    val collectedAt: String,
)
