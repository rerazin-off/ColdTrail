package com.example.mrdetective.domain.usecases.notebook

import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.repositories.NotebookRepository
import javax.inject.Inject

/** Removes a note from the investigation notebook. */
class DeleteNoteUseCase
    @Inject
    constructor(
        private val repository: NotebookRepository,
    ) {
        /** Deletes the note identified by the caller. */
        suspend operator fun invoke(noteId: Int): Result<Unit> {
            if (noteId <= 0) {
                return Result.failure(
                    DomainError.Validation("Некорректный ID записки"),
                )
            }

            return repository.deleteNote(noteId)
        }
    }
