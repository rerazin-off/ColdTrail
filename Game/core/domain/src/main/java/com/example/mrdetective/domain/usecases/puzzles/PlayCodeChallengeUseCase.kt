package com.example.mrdetective.domain.usecases.puzzles

import com.example.mrdetective.domain.models.DomainError
import javax.inject.Inject

const val CODE_CHALLENGE_LENGTH = 4

/**
 * Проверяет формат числового ответа и отправляет его на проверку.
 *
 * @param playPuzzle Сценарий передачи ответа в репозиторий головоломок.
 */
class PlayCodeChallengeUseCase
    @Inject
    constructor(
        private val playPuzzle: PlayPuzzleUseCase,
    ) {
        /**
         * Отклоняет ответ, если он состоит не из четырёх цифр.
         *
         * @param investigationId Ненулевой идентификатор расследования. Отрицательный идентификатор относится
         *   к локальной записи.
         * @param puzzleId Идентификатор головоломки, ответ на которую проверяется.
         * @param answer Четыре цифры в виде строки. Начальный ноль сохраняется.
         * @return Результат проверки кода или ошибка, в том числе если ответ ожидает синхронизации.
         */
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
