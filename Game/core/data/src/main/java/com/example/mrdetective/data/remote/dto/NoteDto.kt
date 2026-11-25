package com.example.mrdetective.data.remote.dto

import com.google.gson.annotations.SerializedName

/** Запись заметки из REST API Supabase. */
data class NoteDto(
    val id: Int,
    @SerializedName("investigation_id") val investigationId: Int,
    val text: String,
    @SerializedName("create_at") val createdAt: String,
    @SerializedName("update_at") val updatedAt: String,
)

/** Поля для создания или обновления заметки расследования. */
data class NoteInsertDto(
    @SerializedName("investigation_id") val investigationId: Int,
    val text: String,
)
