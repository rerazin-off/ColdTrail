package com.example.mrdetective.domain.models

/**
 * Описывает улику и её необязательное изображение.
 *
 * @property id Идентификатор улики.
 * @property caseId Идентификатор дела, к которому относится запись или операция.
 * @property title Название улики.
 * @property description Текстовое описание объекта.
 * @property image Путь к изображению в хранилище, если оно задано.
 * @property createdAt Время создания записи.
 */
data class Clue(
    val id: Int,
    val caseId: Int,
    val title: String,
    val description: String,
    val image: String?,
    val createdAt: String,
)
