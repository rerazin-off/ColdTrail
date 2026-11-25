package com.example.mrdetective.domain.repositories

import com.example.mrdetective.domain.models.DialogueMessage

/** Предоставляет историю диалога и обмен репликами с подозреваемым. */
interface DialogueRepository {
    /** Возвращает историю диалога для указанного дела и подозреваемого. */
    suspend fun getHistory(
        caseId: Int,
        suspectId: Int,
    ): Result<List<DialogueMessage>>

    /** Отправляет вопрос игрока и возвращает ответ подозреваемого. */
    suspend fun sendMessage(
        caseId: Int,
        suspectId: Int,
        message: String,
    ): Result<DialogueMessage>

    /** Предъявляет подозреваемому улику из инвентаря и возвращает его реакцию. */
    suspend fun presentClue(
        caseId: Int,
        suspectId: Int,
        clueId: Int,
        message: String = "Предъявляю улику №$clueId",
    ): Result<DialogueMessage>
}
