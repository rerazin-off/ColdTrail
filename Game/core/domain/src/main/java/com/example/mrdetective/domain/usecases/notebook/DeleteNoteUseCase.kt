package com.example.mrdetective.domain.usecases.notebook

import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.repositories.NotebookRepository
import javax.inject.Inject

/** Удаляет заметку из блокнота расследования. */
class DeleteNoteUseCase
    @Inject
    constructor(
        private val repository: NotebookRepository,
    ) {
        /** Удаляет заметку с переданным идентификатором. */
        suspend operator fun invoke(noteId: Int): Result<Unit> {
            if (noteId <= 0) {
                return Result.failure(
                    DomainError.Validation("Некорректный ID записки"),
                )
            }

            return repository.deleteNote(noteId)
        }
    }
