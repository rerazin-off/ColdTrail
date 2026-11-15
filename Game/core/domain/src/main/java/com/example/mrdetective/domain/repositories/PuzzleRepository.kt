package com.example.mrdetective.domain.repositories

import com.example.mrdetective.domain.models.Puzzle

interface PuzzleRepository  {
    suspend fun getPuzzle(id: Int): Result<Puzzle>
    suspend fun markSolved(id: Int): Result<Unit>
    fun checkAnswer(investigationId: Int, puzzleId: Int, answer: String): Result<Boolean>
}