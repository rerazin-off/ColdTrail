package com.example.mrdetective.domain.models

/** Stores the player's note for an investigation, with its timestamps. */
data class Note(
    val id: Int,
    val investigationId: Int,
    val text: String,
    val createdAt: String,
    val updatedAt: String,
)
