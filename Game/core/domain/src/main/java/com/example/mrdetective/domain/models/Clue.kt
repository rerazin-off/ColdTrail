package com.example.mrdetective.domain.models

/** Describes a piece of investigation evidence and its optional image. */
data class Clue(
    val id: Int,
    val caseId: Int,
    val title: String,
    val description: String,
    val image: String?,
    val createdAt: String,
)
