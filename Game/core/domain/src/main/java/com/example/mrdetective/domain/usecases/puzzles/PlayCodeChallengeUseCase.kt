package com.example.mrdetective.domain.usecases.puzzles

import com.example.mrdetective.domain.models.DomainError
import javax.inject.Inject

const val CODE_CHALLENGE_LENGTH = 4

/** Проверяет формат числового ответа и отправляет его на проверку. */
class PlayCodeChallengeUseCase
    @Inject
    constructor(
        private val playPuzzle: PlayPuzzleUseCase,
    ) {
        /** Отклоняет ответ, если он состоит не из четырёх цифр. */
        suspend operator fun invoke(
            investigationId: Int,
            puzzleId: Int,
            answer: String,
        ): Result<Boolean> {
            if (answer.length != CODE_CHALLENGE_LENGTH || answer.any { it !in '0'..'9' }) {
                return Result.failure(DomainError.Validation("Введите четыре цифры."))
            }
            return playPuzzle(investigationId, puzzleId, answer)
        }
    }
