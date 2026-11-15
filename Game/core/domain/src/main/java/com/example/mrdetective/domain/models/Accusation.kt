package com.example.mrdetective.domain.models

data class Accusation(
    val id: Int,
    val caseId: Int,
    val suspectId: Int,
    val isCorrect: Boolean? = null,
    val explanation: String = "",
)
