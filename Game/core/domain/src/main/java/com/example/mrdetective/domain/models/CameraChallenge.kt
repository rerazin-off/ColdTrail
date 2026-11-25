package com.example.mrdetective.domain.models

/** Describes a camera puzzle and the clue it can award. */
data class CameraChallenge(
    val id: Int,
    val investigationId: Int,
    val title: String,
    val hint: String?,
    val clueId: Int,
    val clueTitle: String,
    val clueDescription: String,
    val imageUrl: String?,
    val pendingOrCompleted: Boolean,
    val rewardClaimed: Boolean,
)
