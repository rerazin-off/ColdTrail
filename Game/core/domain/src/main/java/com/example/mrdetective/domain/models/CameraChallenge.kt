package com.example.mrdetective.domain.models

/**
 * Описывает головоломку с камерой и улику, которую можно получить.
 *
 * @property id Идентификатор головоломки с камерой.
 * @property investigationId Идентификатор расследования. Для локально начатого дела до синхронизации он может
 *   быть отрицательным.
 * @property title Название дела, улики или головоломки для отображения.
 * @property hint Подсказка к головоломке, если она задана.
 * @property clueId Идентификатор улики.
 * @property clueTitle Название улики, доступной в этой головоломке.
 * @property clueDescription Описание улики, которое показывается после получения.
 * @property imageUrl Адрес изображения, доступного для показа, если он известен.
 * @property pendingOrCompleted Уже поставлено ли решение головоломки в очередь или подтверждено.
 * @property rewardClaimed Уже ли получена улика за головоломку.
 */
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
