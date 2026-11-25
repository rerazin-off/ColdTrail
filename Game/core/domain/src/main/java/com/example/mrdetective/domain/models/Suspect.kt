package com.example.mrdetective.domain.models

/** Contains the public information about a suspect available to the player. */
data class Suspect(
    val id: Int,
    val caseId: Int,
    val name: String,
    val profile: String,
)
