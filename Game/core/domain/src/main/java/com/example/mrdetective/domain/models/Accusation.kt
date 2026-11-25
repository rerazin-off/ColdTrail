package com.example.mrdetective.domain.models

/** Records the submitted suspect and the server's verdict for an investigation. */
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
