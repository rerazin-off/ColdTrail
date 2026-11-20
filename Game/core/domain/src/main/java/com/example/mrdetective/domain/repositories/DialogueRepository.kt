package com.example.mrdetective.domain.repositories

import com.example.mrdetective.domain.models.DialogueMessage

interface DialogueRepository {
    suspend fun getHistory(
        caseId: Int,
        suspectId: Int,
    ): Result<List<DialogueMessage>>

    suspend fun sendMessage(
        caseId: Int,
        suspectId: Int,
        message: String,
    ): Result<DialogueMessage>

    suspend fun presentClue(
        caseId: Int,
        suspectId: Int,
        clueId: Int,
        message: String = "Предъявляю улику №$clueId",
    ): Result<DialogueMessage>
}
