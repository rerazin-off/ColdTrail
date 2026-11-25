package com.example.mrdetective.data.remote.dto

import com.google.gson.annotations.SerializedName

/** Case row returned by the Supabase REST API. */
data class CaseDto(
    val id: Int,
    val title: String,
    val introduction: String,
    val difficulty: Int,
    @SerializedName("create_at") val createdAt: String? = null,
)
