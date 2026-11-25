package com.example.mrdetective.data.local.entity

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

/** Room row containing the cached public data of a case. */
@Entity(tableName = "cases")
data class CaseEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val introduction: String,
    val difficulty: Int = 1,
    val createdAt: String = "",
    val cachedAt: Long = System.currentTimeMillis(),
)

/** Room row containing a suspect profile and its case association. */
@Entity(tableName = "suspects", indices = [Index("caseId")])
data class SuspectEntity(
    @PrimaryKey val id: Int,
    val caseId: Int,
    val name: String,
    val profile: String,
    val photo: String? = null,
    val createdAt: String = "",
)

/** Room row containing a clue's description and optional image. */
@Entity(tableName = "clues", indices = [Index("caseId")])
data class ClueEntity(
    @PrimaryKey val id: Int,
    val caseId: Int,
    val title: String,
    val description: String,
    val image: String? = null,
    val createdAt: String = "",
)

/** Room row representing the progress of a player in one case. */
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

/** Room row associating a collected clue with an investigation. */
@Entity(tableName = "collected_clues", primaryKeys = ["investigationId", "clueId"])
data class CollectedClueEntity(
    val investigationId: Int,
    val clueId: Int,
    val collectedAt: String,
    val synced: Boolean = false,
)

/** Room row associating a solved puzzle with an investigation. */
@Entity(tableName = "solved_puzzles", primaryKeys = ["investigationId", "puzzleId"])
data class SolvedPuzzleEntity(
    val investigationId: Int,
    val puzzleId: Int,
    val solvedAt: String,
    val synced: Boolean = false,
)

/** Room row for a dialogue message, including its local delivery state. */
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

/** Room row containing the player's note and its synchronization status. */
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

/** Room row for public puzzle metadata and its possible clue reward. */
@Entity(tableName = "puzzles", indices = [Index("caseId")])
data class PuzzleEntity(
    @PrimaryKey val id: Int,
    val caseId: Int,
    val kind: String,
    val title: String,
    val hint: String?,
    val rewardClueId: Int?,
)
