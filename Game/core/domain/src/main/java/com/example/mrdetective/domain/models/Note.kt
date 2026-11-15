package com.example.mrdetective.domain.models

data class Note(
    val id: Int,
    val investigationId: Int,
    val text: String,
    val createdAt: String,
    val updatedAt: String,
)
