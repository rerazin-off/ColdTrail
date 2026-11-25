package com.example.mrdetective.domain.models

/**
 * Хранит статус расследования, найденные улики и решённые головоломки.
 *
 * @property id Идентификатор расследования.
 * @property userId Идентификатор владельца записи или активного игрока.
 * @property caseId Идентификатор дела, к которому относится запись или операция.
 * @property collectedClueIds Идентификаторы улик, которые игрок уже собрал.
 * @property status Текущее состояние дела или расследования.
 */
data class InvestigationProgress(
    val id: Int,
    val userId: String,
    val caseId: Int,
    val collectedClueIds: List<Int>,
    val status: String,
)
