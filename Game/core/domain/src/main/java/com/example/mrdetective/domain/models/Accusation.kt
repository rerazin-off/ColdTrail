package com.example.mrdetective.domain.models

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
