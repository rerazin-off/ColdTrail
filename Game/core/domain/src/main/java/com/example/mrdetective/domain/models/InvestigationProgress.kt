package com.example.mrdetective.domain.models

/** Хранит статус расследования, найденные улики и решённые головоломки. */
data class InvestigationProgress(
    val id: Int,
    val userId: String,
    val caseId: Int,
    val collectedClueIds: List<Int>,
    val status: String,
)
