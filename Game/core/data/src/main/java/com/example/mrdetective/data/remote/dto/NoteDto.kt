package com.example.mrdetective.data.remote.dto

import com.google.gson.annotations.SerializedName

data class NoteDto(
    val id: Int,
    @SerializedName("investigation_id") val investigationId: Int,
    val text: String,
    @SerializedName("create_at") val createdAt: String,
    @SerializedName("update_at") val updatedAt: String,
)

data class NoteInsertDto(
    @SerializedName("investigation_id") val investigationId: Int,
    val text: String,
)
