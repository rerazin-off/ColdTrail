package com.example.mrdetective.domain.repositories

import com.example.mrdetective.domain.models.DialogueMessage

/** Provides conversation history and exchanges with a suspect. */
interface DialogueRepository {
    /** Returns the conversation history for the specified case and suspect. */
    suspend fun getHistory(
        caseId: Int,
        suspectId: Int,
    ): Result<List<DialogueMessage>>

    /** Sends the player's question and returns the suspect's reply. */
    suspend fun sendMessage(
        caseId: Int,
        suspectId: Int,
        message: String,
    ): Result<DialogueMessage>

    /** Presents an inventory clue to a suspect and returns their response. */
    suspend fun presentClue(
        caseId: Int,
        suspectId: Int,
        clueId: Int,
        message: String = "Предъявляю улику №$clueId",
    ): Result<DialogueMessage>
}
