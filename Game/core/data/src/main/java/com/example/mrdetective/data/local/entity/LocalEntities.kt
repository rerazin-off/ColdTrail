package com.example.mrdetective.data.local.entity

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

/**
 * Запись Room с кешированными публичными данными дела.
 *
 * @property id Идентификатор дела.
 * @property title Сохранённое название дела.
 * @property introduction Вступительный текст дела.
 * @property difficulty Уровень сложности дела.
 * @property createdAt Время создания записи.
 * @property cachedAt Время записи в кеш в миллисекундах Unix.
 */
@Entity(tableName = "cases")
data class CaseEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val introduction: String,
    val difficulty: Int = 1,
    val createdAt: String = "",
    val cachedAt: Long = System.currentTimeMillis(),
)

/**
 * Запись Room с профилем подозреваемого и его привязкой к делу.
 *
 * @property id Идентификатор подозреваемого.
 * @property caseId Идентификатор дела, к которому относится запись или операция.
 * @property name Отображаемое имя подозреваемого.
 * @property profile Публичное описание подозреваемого.
 * @property photo Путь к фотографии подозреваемого, если она задана.
 * @property createdAt Время создания записи.
 */
@Entity(tableName = "suspects", indices = [Index("caseId")])
data class SuspectEntity(
    @PrimaryKey val id: Int,
    val caseId: Int,
    val name: String,
    val profile: String,
    val photo: String? = null,
    val createdAt: String = "",
)

/**
 * Запись Room с описанием улики и необязательным изображением.
 *
 * @property id Идентификатор улики.
 * @property caseId Идентификатор дела, к которому относится запись или операция.
 * @property title Сохранённое название улики.
 * @property description Текстовое описание объекта.
 * @property image Путь к изображению в хранилище, если оно задано.
 * @property createdAt Время создания записи.
 */
@Entity(tableName = "clues", indices = [Index("caseId")])
data class ClueEntity(
    @PrimaryKey val id: Int,
    val caseId: Int,
    val title: String,
    val description: String,
    val image: String? = null,
    val createdAt: String = "",
)

/**
 * Запись Room о прогрессе игрока по одному делу.
 *
 * @property id Идентификатор расследования. До синхронизации локальная запись использует отрицательное
 *   значение.
 * @property userId Идентификатор владельца записи или активного игрока.
 * @property caseId Идентификатор дела, к которому относится запись или операция.
 * @property status Текущее состояние дела или расследования.
 * @property startedAt Время начала расследования.
 * @property finishedAt Время завершения расследования, если оно завершено.
 * @property updatedAt Время последнего обновления записи.
 */
@Entity(tableName = "investigations", indices = [Index("caseId"), Index("userId")])
data class InvestigationEntity(
    @PrimaryKey val id: Int,
    val userId: String,
    val caseId: Int,
    val status: String = "in_progress",
    val startedAt: String = "",
    val finishedAt: String? = null,
    val updatedAt: Long = System.currentTimeMillis(),
)

/**
 * Запись Room, связывающая найденную улику с расследованием.
 *
 * @property investigationId Идентификатор расследования. Для локально начатого дела до синхронизации он может
 *   быть отрицательным.
 * @property clueId Идентификатор улики.
 * @property collectedAt Время получения улики.
 * @property synced Подтверждена ли локальная запись сервером.
 */
@Entity(tableName = "collected_clues", primaryKeys = ["investigationId", "clueId"])
data class CollectedClueEntity(
    val investigationId: Int,
    val clueId: Int,
    val collectedAt: String,
    val synced: Boolean = false,
)

/**
 * Запись Room, связывающая решённую головоломку с расследованием.
 *
 * @property investigationId Идентификатор расследования. Для локально начатого дела до синхронизации он может
 *   быть отрицательным.
 * @property puzzleId Идентификатор головоломки.
 * @property solvedAt Время, когда решение было подтверждено.
 * @property synced Подтверждена ли локальная запись сервером.
 */
@Entity(tableName = "solved_puzzles", primaryKeys = ["investigationId", "puzzleId"])
data class SolvedPuzzleEntity(
    val investigationId: Int,
    val puzzleId: Int,
    val solvedAt: String,
    val synced: Boolean = false,
)

/**
 * Запись Room с сообщением и его локальным статусом доставки.
 *
 * @property id Идентификатор реплики.
 * @property investigationId Идентификатор расследования. Для локально начатого дела до синхронизации он может
 *   быть отрицательным.
 * @property caseId Идентификатор дела, к которому относится запись или операция.
 * @property suspectId Идентификатор подозреваемого.
 * @property author Автор реплики: игрок или подозреваемый.
 * @property text Текст локально сохранённой реплики.
 * @property createdAt Время создания записи.
 * @property synced Подтверждена ли локальная запись сервером.
 */
@Entity(tableName = "dialogue_messages", indices = [Index("investigationId", "suspectId")])
data class DialogueMessageEntity(
    @PrimaryKey val id: Long,
    val investigationId: Int,
    val caseId: Int,
    val suspectId: Int,
    val author: String,
    val text: String,
    val createdAt: String,
    val synced: Boolean = false,
)

/**
 * Запись Room с заметкой игрока и её статусом синхронизации.
 *
 * @property id Идентификатор заметки. Для записи, которая ещё не отправлена, он отрицательный.
 * @property investigationId Идентификатор расследования. Для локально начатого дела до синхронизации он может
 *   быть отрицательным.
 * @property text Текст заметки, сохранённой на устройстве.
 * @property createdAt Время создания записи.
 * @property updatedAt Время последнего обновления записи.
 * @property localUpdatedAt Временная метка для сортировки локальных записей.
 * @property synced Подтверждена ли локальная запись сервером.
 */
@Entity(tableName = "notes", indices = [Index("investigationId")])
data class NoteEntity(
    @PrimaryKey val id: Int,
    val investigationId: Int,
    val text: String,
    val createdAt: String,
    val updatedAt: String,
    val localUpdatedAt: Long = System.currentTimeMillis(),
    val synced: Boolean = true,
)

/**
 * Запись Room с публичными метаданными головоломки и её возможной наградой.
 *
 * @property id Идентификатор головоломки.
 * @property caseId Идентификатор дела, к которому относится запись или операция.
 * @property kind Тип головоломки, сохранённый в кеше.
 * @property title Название дела, улики или головоломки для отображения.
 * @property hint Подсказка к головоломке, если она задана.
 * @property rewardClueId Идентификатор улики, выдаваемой за головоломку.
 */
@Entity(tableName = "puzzles", indices = [Index("caseId")])
data class PuzzleEntity(
    @PrimaryKey val id: Int,
    val caseId: Int,
    val kind: String,
    val title: String,
    val hint: String?,
    val rewardClueId: Int?,
)

/**
 * Сохраняет готовый сигнал Морзе для показа без сети.
 *
 * @property puzzleId Идентификатор головоломки.
 * @property caseId Идентификатор дела, к которому относится запись или операция.
 * @property signal Сигнал Морзе в виде точек, тире и разделителей.
 */
@Entity(tableName = "morse_signals", indices = [Index("caseId")])
data class MorseSignalEntity(
    @PrimaryKey val puzzleId: Int,
    val caseId: Int,
    val signal: String,
)
