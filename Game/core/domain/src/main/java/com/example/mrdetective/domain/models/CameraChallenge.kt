package com.example.mrdetective.domain.models

/** Описывает головоломку с камерой и улику, которую можно получить. */
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
