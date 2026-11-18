package com.example.mrdetective.domain.repositories

import com.example.mrdetective.domain.models.Clue

interface ClueRepository {
    suspend fun getClue(id: Int): Result<Clue>

    suspend fun getCollectedClues(caseId: Int): Result<List<Clue>>

    suspend fun collectClue(clueId: Int): Result<Unit>
}
