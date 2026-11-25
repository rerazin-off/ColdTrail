package com.example.mrdetective.domain.models

/**
 * Описывает дело и состояние его расследования игроком.
 *
 * @property id Идентификатор дела.
 * @property title Название дела.
 * @property introduction Вступительный текст дела.
 * @property objective Цель, которую игрок должен выполнить для расследования.
 * @property status Текущее состояние дела или расследования.
 * @property clueIds Идентификаторы всех известных улик дела. `null` означает, что полный список ещё не
 *   загружен.
 */
data class Case(
    val id: Int,
    val title: String,
    val introduction: String,
    val objective: String = "",
    val status: String = "available",
    val clueIds: Set<Int>? = null,
)
