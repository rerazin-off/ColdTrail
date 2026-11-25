package com.example.mrdetective.data.remote.dto

import com.google.gson.annotations.SerializedName

/** Clue row returned by the Supabase REST API. */
data class ClueDto(
    val id: Int,
    @SerializedName("case_id") val caseId: Int,
    val title: String,
    val description: String,
    val image: String?,
    @SerializedName("create_at") val createdAt: String? = null,
)
