package com.example.mrdetective.data.mapper

import com.example.mrdetective.data.remote.dto.CaseDto
import com.example.mrdetective.data.remote.dto.ClueDto
import com.example.mrdetective.data.remote.dto.NoteDto
import com.example.mrdetective.data.remote.dto.SuspectDto
import com.example.mrdetective.domain.models.Case
import com.example.mrdetective.domain.models.Clue
import com.example.mrdetective.domain.models.Note
import com.example.mrdetective.domain.models.Suspect

/** Maps a case record, with app-side defaults for objective and status and no difficulty or creation date. */
fun CaseDto.toDomain() =
    Case(
        id = id,
        title = title,
        introduction = introduction,
        objective = "",
        status = "available",
    )

/** Maps the public fields of a suspect record to the domain model. */
fun SuspectDto.toDomain() = Suspect(id = id, caseId = caseId, name = name, profile = profile)

/** Maps a clue and its image reference, using an empty timestamp if the API omits it. */
fun ClueDto.toDomain() =
    Clue(
        id = id,
        caseId = caseId,
        title = title,
        description = description,
        image = image,
        createdAt = createdAt.orEmpty(),
    )

/** Maps the note identifier, investigation, text, and timestamps. */
fun NoteDto.toDomain() =
    Note(
        id = id,
        investigationId = investigationId,
        text = text,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
