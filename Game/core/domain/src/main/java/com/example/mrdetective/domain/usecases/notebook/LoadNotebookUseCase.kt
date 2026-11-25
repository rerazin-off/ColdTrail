package com.example.mrdetective.domain.usecases.notebook

import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.models.Note
import com.example.mrdetective.domain.repositories.NotebookRepository
import javax.inject.Inject

/**
 * Загружает текущую заметку расследования.
 *
 * @param repository Репозиторий чтения, записи и удаления заметок.
 */
class LoadNotebookUseCase
    @Inject
    constructor(
        private val repository: NotebookRepository,
    ) {
        /**
         * Загружает последнюю заметку расследования.
         *
         * @param investigationId Ненулевой идентификатор расследования. Отрицательный идентификатор относится
         *   к локальной записи.
         * @return Последняя сохранённая заметка или ошибка, в том числе если заметок пока нет.
         */
        suspend operator fun invoke(investigationId: Int): Result<Note> {
            if (investigationId == 0) {
                return Result.failure(
                    DomainError.Validation("Некорректный ID расследования"),
                )
            }

            return repository.getNote(investigationId)
        }
    }
