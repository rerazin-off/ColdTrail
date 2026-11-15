package com.example.mrdetective.domain.models

data class InvestigationProgress(
    val id: Int,
    val userId: String,
    val caseId: Int,
    val collectedClueIds: List<Int>,
    val status: String,
)
