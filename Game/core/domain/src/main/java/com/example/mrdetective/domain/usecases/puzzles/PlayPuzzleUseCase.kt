package com.example.mrdetective.domain.usecases.puzzles

import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.repositories.PuzzleRepository
import javax.inject.Inject

/**
 * Отправляет ответ через репозиторий головоломок.
 *
 * @param repository Репозиторий проверки ответов и результатов головоломок.
 */
class PlayPuzzleUseCase
    @Inject
    constructor(
        private val repository: PuzzleRepository,
    ) {
        /**
         * Передаёт ответ для проверки в репозиторий головоломок.
         *
         * @param investigationId Ненулевой идентификатор расследования. Отрицательный идентификатор относится
         *   к локальной записи.
         * @param puzzleId Идентификатор головоломки, ответ на которую проверяется.
         * @param answer Ответ игрока.
         * @return Результат проверки ответа или ошибка, в том числе если ответ ожидает синхронизации.
         */
        suspend operator fun invoke(
            investigationId: Int,
            puzzleId: Int,
            answer: String,
        ): Result<Boolean> {
            if (
                investigationId == 0 ||
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
