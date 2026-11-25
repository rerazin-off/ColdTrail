package com.example.mrdetective.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Запись дела из REST API Supabase.
 *
 * @property id Идентификатор дела.
 * @property title Название дела из Supabase.
 * @property introduction Вступительный текст дела.
 * @property difficulty Уровень сложности дела.
 * @property createdAt Время создания записи.
 */
data class CaseDto(
    val id: Int,
    val title: String,
    val introduction: String,
    val difficulty: Int,
    @SerializedName("create_at") val createdAt: String? = null,
)
