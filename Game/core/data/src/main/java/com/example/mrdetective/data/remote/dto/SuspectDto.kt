package com.example.mrdetective.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Публичная запись подозреваемого из REST API Supabase.
 *
 * @property id Идентификатор подозреваемого.
 * @property caseId Идентификатор дела, к которому относится запись или операция.
 * @property name Отображаемое имя подозреваемого.
 * @property profile Публичное описание подозреваемого.
 * @property photo Путь к фотографии подозреваемого, если она задана.
 * @property createdAt Время создания записи.
 */
data class SuspectDto(
    val id: Int,
    @SerializedName("case_id") val caseId: Int,
    val name: String,
    val profile: String,
    val photo: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
)
