package com.example.mrdetective.data.remote.dto

import com.google.gson.annotations.SerializedName

/** Server representation of a camera puzzle and its clue reward. */
data class CameraChallengeDto(
    val id: Int,
    val title: String,
    val hint: String?,
    @SerializedName("reward_clue") val rewardClueId: Int,
    val solved: Boolean,
    @SerializedName("reward_claimed") val rewardClaimed: Boolean,
)

/** Server response to a clue capture or scene-search submission. */
data class CameraCaptureDto(
    @SerializedName("p_investigation") val investigationId: Int,
    val puzzleId: Int,
    val clueId: Int,
    val filePath: String,
)
