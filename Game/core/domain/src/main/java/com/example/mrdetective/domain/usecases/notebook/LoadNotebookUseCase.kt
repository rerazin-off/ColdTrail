package com.example.mrdetective.domain.usecases.notebook

import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.models.Note
import com.example.mrdetective.domain.repositories.NotebookRepository
import javax.inject.Inject

/** Loads the current note for an investigation. */
class LoadNotebookUseCase
    @Inject
    constructor(
        private val repository: NotebookRepository,
    ) {
        /** Returns the saved note associated with the investigation, if any. */
        suspend operator fun invoke(investigationId: Int): Result<Note> {
            if (investigationId <= 0) {
                return Result.failure(
                    DomainError.Validation("Некорректный ID расследования"),
                )
            }

            return repository.getNote(investigationId)
        }
    }
