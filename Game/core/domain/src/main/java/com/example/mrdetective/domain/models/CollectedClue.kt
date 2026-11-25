package com.example.mrdetective.domain.models

/** Связывает улику с расследованием, в котором её нашли. */
data class CollectedClue(
    val investigationId: Int,
    val clueId: Int,
    val collectedAt: String,
)
