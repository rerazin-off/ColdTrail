package com.example.mrdetective.domain.repositories

import com.example.mrdetective.domain.models.Clue

/** Provides clue descriptions and records clues collected in an investigation. */
interface ClueRepository {
    /** Looks up a clue by its identifier. */
    suspend fun getClue(id: Int): Result<Clue>

    /** Returns the clues collected for the specified investigation. */
    suspend fun getCollectedClues(caseId: Int): Result<List<Clue>>

    /** Records a clue as collected in the specified investigation. */
    suspend fun collectClue(clueId: Int): Result<Unit>
}
