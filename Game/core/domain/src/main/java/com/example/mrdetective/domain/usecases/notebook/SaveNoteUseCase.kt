package com.example.mrdetective.domain.usecases.notebook

import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.models.Note
import com.example.mrdetective.domain.repositories.NotebookRepository
import javax.inject.Inject

/**
 * Проверяет и сохраняет заметку игрока по расследованию.
 *
 * @param repository Репозиторий чтения, записи и удаления заметок.
 */
class SaveNoteUseCase
    @Inject
    constructor(
        private val repository: NotebookRepository,
    ) {
        /**
         * Проверяет длину текста и сохраняет заметку в блокноте.
         *
         * @param investigationId Ненулевой идентификатор расследования. Отрицательный идентификатор относится
         *   к локальной записи.
         * @param text Непустой текст заметки длиной до 1000 символов.
         * @return Записанная на устройстве заметка или ошибка сохранения.
         */
        suspend operator fun invoke(
            investigationId: Int,
            text: String,
        ): Result<Note> {
            if (investigationId == 0 || text.isBlank()) {
                return Result.failure(
                    DomainError.Validation("Некорректные данные заметки"),
                )
            }

            if (text.length > 1000) {
                return Result.failure(
                    DomainError.Validation("Заметка не должна превышать 1000 символов"),
                )
            }

            return repository.saveNote(investigationId, text)
        }
    }
