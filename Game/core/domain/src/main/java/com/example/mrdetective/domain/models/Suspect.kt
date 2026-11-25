package com.example.mrdetective.domain.models

/** Содержит публичные данные подозреваемого, доступные игроку. */
data class Suspect(
    val id: Int,
    val caseId: Int,
    val name: String,
    val profile: String,
)
