package com.example.mrdetective.domain.models

data class CameraChallenge(
    val id: Int,
    val investigationId: Int,
    val title: String,
    val hint: String?,
    val clueId: Int,
    val clueTitle: String,
    val imageUrl: String?,
    val pendingOrCompleted: Boolean,
    val rewardClaimed: Boolean,
)
