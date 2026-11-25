package com.example.mrdetective.domain.usecases.notebook

import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.models.Note
import com.example.mrdetective.domain.repositories.NotebookRepository
import javax.inject.Inject

/** Загружает текущую заметку расследования. */
class LoadNotebookUseCase
    @Inject
    constructor(
        private val repository: NotebookRepository,
    ) {
        /** Возвращает сохранённую заметку расследования, если она есть. */
        suspend operator fun invoke(investigationId: Int): Result<Note> {
            if (investigationId <= 0) {
                return Result.failure(
                    DomainError.Validation("Некорректный ID расследования"),
                )
            }

            return repository.getNote(investigationId)
        }
    }
