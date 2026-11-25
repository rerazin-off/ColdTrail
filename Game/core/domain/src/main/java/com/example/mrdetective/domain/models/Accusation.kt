package com.example.mrdetective.domain.models

/** Хранит выбранного подозреваемого и серверный вердикт расследования. */
data class Accusation(
    val id: Int,
    val caseId: Int,
    val suspectId: Int,
    val isCorrect: Boolean? = null,
    val explanation: String = "",
    val culpritId: Int? = null,
    val selectedTruth: String? = null,
    val culpritTruth: String? = null,
)
