package com.example.mrdetective.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Серверная модель головоломки с камерой и её улики-награды.
 *
 * @property id Идентификатор головоломки с камерой.
 * @property title Название дела, улики или головоломки для отображения.
 * @property hint Подсказка к головоломке, если она задана.
 * @property rewardClueId Идентификатор улики, выдаваемой за головоломку.
 * @property solved Подтверждено ли правильное решение головоломки.
 * @property rewardClaimed Уже ли получена улика за головоломку.
 */
data class CameraChallengeDto(
    val id: Int,
    val title: String,
    val hint: String?,
    @SerializedName("reward_clue") val rewardClueId: Int,
    val solved: Boolean,
    @SerializedName("reward_claimed") val rewardClaimed: Boolean,
)

/**
 * Данные из локальной очереди для отправки снимка или выбора предмета на сцене.
 *
 * @property investigationId Идентификатор расследования. Для локально начатого дела до синхронизации он может
 *   быть отрицательным.
 * @property puzzleId Идентификатор головоломки.
 * @property clueId Идентификатор улики.
 * @property filePath Путь к локальному фото. Пустая строка применяется для поиска на сцене без снимка.
 */
data class CameraCaptureDto(
    @SerializedName("p_investigation") val investigationId: Int,
    val puzzleId: Int,
    val clueId: Int,
    val filePath: String,
)
