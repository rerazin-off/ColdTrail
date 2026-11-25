package com.example.mrdetective.data.remote.dto

import com.google.gson.annotations.SerializedName

/** Серверная модель головоломки с камерой и её улики-награды. */
data class CameraChallengeDto(
    val id: Int,
    val title: String,
    val hint: String?,
    @SerializedName("reward_clue") val rewardClueId: Int,
    val solved: Boolean,
    @SerializedName("reward_claimed") val rewardClaimed: Boolean,
)

/** Ответ сервера на отправку снимка или выбор предмета на сцене. */
data class CameraCaptureDto(
    @SerializedName("p_investigation") val investigationId: Int,
    val puzzleId: Int,
    val clueId: Int,
    val filePath: String,
)
