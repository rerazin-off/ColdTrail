package com.example.mrdetective.domain.models

/** Описывает улику и её необязательное изображение. */
data class Clue(
    val id: Int,
    val caseId: Int,
    val title: String,
    val description: String,
    val image: String?,
    val createdAt: String,
)
