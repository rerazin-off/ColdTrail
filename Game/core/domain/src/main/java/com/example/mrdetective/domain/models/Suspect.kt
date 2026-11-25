package com.example.mrdetective.domain.models

/**
 * Содержит публичные данные подозреваемого, доступные игроку.
 *
 * @property id Идентификатор подозреваемого.
 * @property caseId Идентификатор дела, к которому относится запись или операция.
 * @property name Отображаемое имя подозреваемого.
 * @property profile Публичное описание подозреваемого.
 */
data class Suspect(
    val id: Int,
    val caseId: Int,
    val name: String,
    val profile: String,
)
