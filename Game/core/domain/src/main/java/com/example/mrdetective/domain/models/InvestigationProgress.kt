package com.example.mrdetective.domain.models

/** Captures an investigation's status, collected clues, and solved puzzles. */
data class InvestigationProgress(
    val id: Int,
    val userId: String,
    val caseId: Int,
    val collectedClueIds: List<Int>,
    val status: String,
)
