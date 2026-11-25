package com.example.mrdetective.domain.fakes

import com.example.mrdetective.domain.models.Accusation
import com.example.mrdetective.domain.models.Case
import com.example.mrdetective.domain.models.Clue
import com.example.mrdetective.domain.models.CollectedClue
import com.example.mrdetective.domain.models.DialogueMessage
import com.example.mrdetective.domain.models.InvestigationProgress
import com.example.mrdetective.domain.models.MessageAuthor
import com.example.mrdetective.domain.models.Note
import com.example.mrdetective.domain.models.Suspect

/** Предоставляет данные дел, улик и расследований для юнит-тестов домена. */
object TestData {
    val case = Case(id = 1, title = "Дело", introduction = "Вступление")
    val suspect = Suspect(id = 1, caseId = 1, name = "Иван", profile = "Садовник")
    val clue = Clue(1, 1, "Нож", "Окровавленный нож", null, "2026-01-01")
    val progress =
        InvestigationProgress(
            id = 10,
            userId = "user-1",
            caseId = 1,
            collectedClueIds = emptyList(),
            status = "in_progress",
        )
    val collectedClue = CollectedClue(investigationId = 10, clueId = 1, collectedAt = "2026-01-01")
    val note = Note(1, 10, "Текст", "2026-01-01", "2026-01-01")
    val message = DialogueMessage(1, 1, 1, MessageAuthor.SUSPECT, "Я ничего не знаю", "2026-01-01")
    val accusation = Accusation(id = 1, caseId = 1, suspectId = 1, isCorrect = true)
}
