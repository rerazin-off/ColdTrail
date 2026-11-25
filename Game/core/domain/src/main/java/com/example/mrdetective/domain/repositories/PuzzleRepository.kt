package com.example.mrdetective.domain.repositories

import com.example.mrdetective.domain.models.Puzzle

/** Looks up puzzles, validates solutions, and records completion. */
interface PuzzleRepository {
    /** Looks up the public data for a puzzle. */
    suspend fun getPuzzle(id: Int): Result<Puzzle>

    /** Records a puzzle as solved in the relevant investigation. */
    suspend fun markSolved(id: Int): Result<Unit>

    /** Validates an answer to the selected puzzle. */
    suspend fun checkAnswer(
        investigationId: Int,
        puzzleId: Int,
        answer: String,
    ): Result<Boolean>
}
