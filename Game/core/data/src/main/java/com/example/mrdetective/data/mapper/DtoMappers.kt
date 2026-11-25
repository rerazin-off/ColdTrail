package com.example.mrdetective.data.mapper

import com.example.mrdetective.data.remote.dto.CaseDto
import com.example.mrdetective.data.remote.dto.ClueDto
import com.example.mrdetective.data.remote.dto.NoteDto
import com.example.mrdetective.data.remote.dto.SuspectDto
import com.example.mrdetective.domain.models.Case
import com.example.mrdetective.domain.models.Clue
import com.example.mrdetective.domain.models.Note
import com.example.mrdetective.domain.models.Suspect

/** Создаёт модель дела с начальной целью и статусом. Сложность и дата создания в модель не входят. */
fun CaseDto.toDomain() =
    Case(
        id = id,
        title = title,
        introduction = introduction,
        objective = "",
        status = "available",
    )

/** Переносит публичные поля подозреваемого в доменную модель. */
fun SuspectDto.toDomain() = Suspect(id = id, caseId = caseId, name = name, profile = profile)

/** Переносит данные улики и ссылку на изображение. Если API не вернул дату, подставляет пустую строку. */
fun ClueDto.toDomain() =
    Clue(
        id = id,
        caseId = caseId,
        title = title,
        description = description,
        image = image,
        createdAt = createdAt.orEmpty(),
    )

/** Переносит идентификатор заметки, расследование, текст и временные метки. */
fun NoteDto.toDomain() =
    Note(
        id = id,
        investigationId = investigationId,
        text = text,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
