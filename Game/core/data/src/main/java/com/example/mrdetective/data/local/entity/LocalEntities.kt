package com.example.mrdetective.data.local.entity

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

/** Запись Room с кешированными публичными данными дела. */
@Entity(tableName = "cases")
data class CaseEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val introduction: String,
    val difficulty: Int = 1,
    val createdAt: String = "",
    val cachedAt: Long = System.currentTimeMillis(),
)

/** Запись Room с профилем подозреваемого и его привязкой к делу. */
@Entity(tableName = "suspects", indices = [Index("caseId")])
data class SuspectEntity(
    @PrimaryKey val id: Int,
    val caseId: Int,
    val name: String,
    val profile: String,
    val photo: String? = null,
    val createdAt: String = "",
)

/** Запись Room с описанием улики и необязательным изображением. */
@Entity(tableName = "clues", indices = [Index("caseId")])
data class ClueEntity(
    @PrimaryKey val id: Int,
    val caseId: Int,
    val title: String,
    val description: String,
    val image: String? = null,
    val createdAt: String = "",
)

/** Запись Room о прогрессе игрока по одному делу. */
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

/** Запись Room, связывающая найденную улику с расследованием. */
@Entity(tableName = "collected_clues", primaryKeys = ["investigationId", "clueId"])
data class CollectedClueEntity(
    val investigationId: Int,
    val clueId: Int,
    val collectedAt: String,
    val synced: Boolean = false,
)

/** Запись Room, связывающая решённую головоломку с расследованием. */
@Entity(tableName = "solved_puzzles", primaryKeys = ["investigationId", "puzzleId"])
data class SolvedPuzzleEntity(
    val investigationId: Int,
    val puzzleId: Int,
    val solvedAt: String,
    val synced: Boolean = false,
)

/** Запись Room с сообщением и его локальным статусом доставки. */
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

/** Запись Room с заметкой игрока и её статусом синхронизации. */
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

/** Запись Room с публичными метаданными головоломки и её возможной наградой. */
@Entity(tableName = "puzzles", indices = [Index("caseId")])
data class PuzzleEntity(
    @PrimaryKey val id: Int,
    val caseId: Int,
    val kind: String,
    val title: String,
    val hint: String?,
    val rewardClueId: Int?,
)
