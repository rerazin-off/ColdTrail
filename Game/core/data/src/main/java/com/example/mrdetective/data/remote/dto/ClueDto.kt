package com.example.mrdetective.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Запись улики из REST API Supabase.
 *
 * @property id Идентификатор улики.
 * @property caseId Идентификатор дела, к которому относится запись или операция.
 * @property title Название улики из Supabase.
 * @property description Текстовое описание объекта.
 * @property image Путь к изображению в хранилище, если оно задано.
 * @property createdAt Время создания записи.
 */
data class ClueDto(
    val id: Int,
    @SerializedName("case_id") val caseId: Int,
    val title: String,
    val description: String,
    val image: String?,
    @SerializedName("create_at") val createdAt: String? = null,
)
