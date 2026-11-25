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

/** Преобразует серверную запись дела для кеша, сохраняя сложность и время создания. */
fun CaseDto.toEntity() = CaseEntity(id, title, introduction, difficulty, createdAt.orEmpty())

/** Создаёт модель дела из кешированного описания. */
fun CaseEntity.toDomain() = Case(id, title, introduction)

/** Преобразует серверный профиль подозреваемого для кеша, сохраняя ссылку на фотографию. */
fun SuspectDto.toEntity() = SuspectEntity(id, caseId, name, profile, photo, createdAt.orEmpty())

/** Создаёт публичную модель подозреваемого из записи кеша. */
fun SuspectEntity.toDomain() = Suspect(id, caseId, name, profile)

/** Преобразует серверные метаданные улики для Room, подставляя пустую строку при отсутствии даты. */
fun ClueDto.toEntity() = ClueEntity(id, caseId, title, description, image, createdAt.orEmpty())

/** Создаёт модель улики из кешированных описания и ссылки на изображение. */
fun ClueEntity.toDomain() = Clue(id, caseId, title, description, image, createdAt)

/** Преобразует серверную заметку для Room, сохраняя её временные метки. */
fun NoteDto.toEntity() =
    NoteEntity(
        id,
        investigationId,
        text,
        createdAt,
        updatedAt,
        localUpdatedAt = runCatching { java.time.Instant.parse(createdAt).toEpochMilli() }.getOrDefault(0L),
    )

/** Создаёт модель заметки из сохранённого текста и временных меток. */
fun NoteEntity.toDomain() = Note(id, investigationId, text, createdAt, updatedAt)

/** Создаёт модель сообщения и преобразует значение автора в доменный тип. */
fun DialogueMessageEntity.toDomain() =
    DialogueMessage(
        id = id.toInt(),
        caseId = caseId,
        suspectId = suspectId,
        author = if (author.equals("suspect", true)) MessageAuthor.SUSPECT else MessageAuthor.PLAYER,
        text = text,
        createdAt = createdAt,
    )

/** Создаёт публичную модель головоломки из кеша. Ответ проверяется на сервере. */
fun PuzzleEntity.toDomain() =
    Puzzle(
        id = id,
        encodedPattern = "$kind:$title${hint?.let { "|$it" } ?: ""}",
        // Политики доступа SQL скрывают ответы от мобильного клиента.
        expectedAnswer = "",
        clueRewardId = rewardClueId,
    )
