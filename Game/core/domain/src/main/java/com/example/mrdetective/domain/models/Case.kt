package com.example.mrdetective.domain.models

/** Describes a case and its investigation status for the player. */
data class Case(
    val id: Int,
    val title: String,
    val introduction: String,
    val objective: String = "",
    val status: String = "available",
    val clueIds: Set<Int>? = null,
)
