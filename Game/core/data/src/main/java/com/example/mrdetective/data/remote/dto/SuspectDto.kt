package com.example.mrdetective.data.remote.dto

import com.google.gson.annotations.SerializedName

/** Public suspect record returned by the Supabase REST API. */
data class SuspectDto(
    val id: Int,
    @SerializedName("case_id") val caseId: Int,
    val name: String,
    val profile: String,
    val photo: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
)
