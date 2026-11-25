package com.example.mrdetective.data.local

import com.example.mrdetective.data.local.entity.CaseEntity
import com.example.mrdetective.data.local.entity.ClueEntity
import com.example.mrdetective.data.local.entity.DialogueMessageEntity
import com.example.mrdetective.data.local.entity.NoteEntity
import com.example.mrdetective.data.local.entity.PuzzleEntity
import com.example.mrdetective.data.local.entity.SuspectEntity
import com.example.mrdetective.data.remote.dto.CaseDto
import com.example.mrdetective.data.remote.dto.ClueDto
import com.example.mrdetective.data.remote.dto.NoteDto
import com.example.mrdetective.data.remote.dto.SuspectDto
import com.example.mrdetective.domain.models.Case
import com.example.mrdetective.domain.models.Clue
import com.example.mrdetective.domain.models.DialogueMessage
import com.example.mrdetective.domain.models.MessageAuthor
import com.example.mrdetective.domain.models.Note
import com.example.mrdetective.domain.models.Puzzle
import com.example.mrdetective.domain.models.Suspect

/** Converts the remote case record to a cache row, retaining the difficulty and creation timestamp. */
fun CaseDto.toEntity() = CaseEntity(id, title, introduction, difficulty, createdAt.orEmpty())

/** Builds a domain case from the locally cached case description. */
fun CaseEntity.toDomain() = Case(id, title, introduction)

/** Converts the remote suspect profile to a cache row, retaining its photo reference. */
fun SuspectDto.toEntity() = SuspectEntity(id, caseId, name, profile, photo, createdAt.orEmpty())

/** Builds the public domain profile from the cached suspect record. */
fun SuspectEntity.toDomain() = Suspect(id, caseId, name, profile)

/** Converts remote clue metadata to a Room row, using an empty timestamp when none is supplied. */
fun ClueDto.toEntity() = ClueEntity(id, caseId, title, description, image, createdAt.orEmpty())

/** Builds a domain clue from the cached description and image reference. */
fun ClueEntity.toDomain() = Clue(id, caseId, title, description, image, createdAt)

/** Converts the remote note to a Room row with its timestamps. */
fun NoteDto.toEntity() = NoteEntity(id, investigationId, text, createdAt, updatedAt)

/** Builds a domain note from the cached text and timestamps. */
fun NoteEntity.toDomain() = Note(id, investigationId, text, createdAt, updatedAt)

/** Builds a domain message and maps the stored author string to an author type. */
fun DialogueMessageEntity.toDomain() =
    DialogueMessage(
        id = id.toInt(),
        caseId = caseId,
        suspectId = suspectId,
        author = if (author.equals("suspect", true)) MessageAuthor.SUSPECT else MessageAuthor.PLAYER,
        text = text,
        createdAt = createdAt,
    )

/** Builds public puzzle information from the cache. Answer validation remains on the server. */
fun PuzzleEntity.toDomain() =
    Puzzle(
        id = id,
        encodedPattern = "$kind:$title${hint?.let { "|$it" } ?: ""}",
        // The SQL permissions intentionally never expose answers to the mobile client.
        expectedAnswer = "",
        clueRewardId = rewardClueId,
    )
