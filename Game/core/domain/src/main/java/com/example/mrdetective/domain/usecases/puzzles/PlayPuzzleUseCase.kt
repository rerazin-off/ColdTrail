package com.example.mrdetective.domain.usecases.puzzles

import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.repositories.PuzzleRepository
import javax.inject.Inject

/** Submits a puzzle answer through the puzzle repository. */
class PlayPuzzleUseCase
    @Inject
    constructor(
        private val repository: PuzzleRepository,
    ) {
        /** Passes an answer to the puzzle repository for validation. */
        suspend operator fun invoke(
            investigationId: Int,
            puzzleId: Int,
            answer: String,
        ): Result<Boolean> {
            if (
                investigationId <= 0 ||
                puzzleId <= 0 ||
                answer.isBlank()
            ) {
                return Result.failure(
                    DomainError.Validation("Некорректные данные ответа"),
                )
            }

            return repository.checkAnswer(
                investigationId,
                puzzleId,
                answer,
            )
        }
    }
