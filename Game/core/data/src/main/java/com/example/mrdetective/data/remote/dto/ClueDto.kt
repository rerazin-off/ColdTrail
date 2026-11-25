package com.example.mrdetective.data.remote.dto

import com.google.gson.annotations.SerializedName

/** Запись улики из REST API Supabase. */
data class ClueDto(
    val id: Int,
    @SerializedName("case_id") val caseId: Int,
    val title: String,
    val description: String,
    val image: String?,
    @SerializedName("create_at") val createdAt: String? = null,
)
