package com.example.mrdetective.domain.models

/** Хранит заметку игрока по расследованию и её временные метки. */
data class Note(
    val id: Int,
    val investigationId: Int,
    val text: String,
    val createdAt: String,
    val updatedAt: String,
)
