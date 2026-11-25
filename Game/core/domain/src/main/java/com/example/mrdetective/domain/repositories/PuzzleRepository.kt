package com.example.mrdetective.domain.repositories

import com.example.mrdetective.domain.models.Puzzle

/** Ищет головоломки, проверяет решения и регистрирует завершение. */
interface PuzzleRepository {
    /** Ищет публичные данные головоломки. */
    suspend fun getPuzzle(id: Int): Result<Puzzle>

    /** Отмечает головоломку решённой в соответствующем расследовании. */
    suspend fun markSolved(id: Int): Result<Unit>

    /** Проверяет ответ на выбранную головоломку. */
    suspend fun checkAnswer(
        investigationId: Int,
        puzzleId: Int,
        answer: String,
    ): Result<Boolean>
}
