package com.example.mrdetective.domain.models

data class Suspect(
    val id: Int,
    val caseId: Int,
    val name: String,
    val profile: String,
)
