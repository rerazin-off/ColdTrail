package com.example.mrdetective.domain.models

/** Описывает дело и состояние его расследования игроком. */
data class Case(
    val id: Int,
    val title: String,
    val introduction: String,
    val objective: String = "",
    val status: String = "available",
    val clueIds: Set<Int>? = null,
)
