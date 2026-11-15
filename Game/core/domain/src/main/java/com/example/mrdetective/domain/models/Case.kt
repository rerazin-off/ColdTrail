package com.example.mrdetective.domain.models

data class Case(
    val id: Int,
    val title: String,
    val introduction: String,
    val objective: String = "",
    val status: String = "available"
)