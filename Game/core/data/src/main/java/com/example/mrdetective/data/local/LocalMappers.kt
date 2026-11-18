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

fun CaseDto.toEntity() = CaseEntity(id, title, introduction, difficulty, createdAt.orEmpty())
fun CaseEntity.toDomain() = Case(id, title, introduction)
fun SuspectDto.toEntity() = SuspectEntity(id, caseId, name, profile, photo, createdAt.orEmpty())
fun SuspectEntity.toDomain() = Suspect(id, caseId, name, profile)
fun ClueDto.toEntity() = ClueEntity(id, caseId, title, description, image, createdAt.orEmpty())
fun ClueEntity.toDomain() = Clue(id, caseId, title, description, image, createdAt)
fun NoteDto.toEntity() = NoteEntity(id, investigationId, text, createdAt, updatedAt)
fun NoteEntity.toDomain() = Note(id, investigationId, text, createdAt, updatedAt)
fun DialogueMessageEntity.toDomain() = DialogueMessage(
    id = id.toInt(), caseId = caseId, suspectId = suspectId,
    author = if (author.equals("suspect", true)) MessageAuthor.SUSPECT else MessageAuthor.PLAYER,
    text = text, createdAt = createdAt,
)


fun PuzzleEntity.toDomain() = Puzzle(
    id = id,
    encodedPattern = "$kind:$title${hint?.let { "|$it" } ?: ""}",
    // The SQL permissions intentionally never expose answers to the mobile client.
    expectedAnswer = "",
    clueRewardId = rewardClueId,
)
