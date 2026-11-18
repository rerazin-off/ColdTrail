package com.example.mrdetective.data.mapper

import com.example.mrdetective.data.remote.dto.CaseDto
import com.example.mrdetective.data.remote.dto.ClueDto
import com.example.mrdetective.data.remote.dto.NoteDto
import com.example.mrdetective.data.remote.dto.SuspectDto
import com.example.mrdetective.domain.models.Case
import com.example.mrdetective.domain.models.Clue
import com.example.mrdetective.domain.models.Note
import com.example.mrdetective.domain.models.Suspect

// Domain.Case has no difficulty/createdAt fields; objective/status are app-side fields, not SQL columns.
fun CaseDto.toDomain() = Case(
    id = id,
    title = title,
    introduction = introduction,
    objective = "",
    status = "available",
)

fun SuspectDto.toDomain() = Suspect(id = id, caseId = caseId, name = name, profile = profile)

fun ClueDto.toDomain() = Clue(
    id = id,
    caseId = caseId,
    title = title,
    description = description,
    image = image,
    createdAt = createdAt.orEmpty(),
)

fun NoteDto.toDomain() = Note(
    id = id,
    investigationId = investigationId,
    text = text,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
