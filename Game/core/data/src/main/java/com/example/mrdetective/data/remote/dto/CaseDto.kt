package com.example.mrdetective.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CaseDto(
    val id: Int,
    val title: String,
    val introduction: String,
    val difficulty: Int,
    @SerializedName("create_at") val createdAt: String? = null,
)