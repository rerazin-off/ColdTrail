package com.example.mrdetective.domain.models

data class Clue(
    val id: Int,
    val caseId: Int,
    val title: String,
    val description: String,
    val image: String?,
    val createdAt: String,
)
