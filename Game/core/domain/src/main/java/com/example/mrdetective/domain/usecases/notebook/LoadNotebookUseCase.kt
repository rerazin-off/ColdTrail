package com.example.mrdetective.domain.usecases.notebook

import com.example.mrdetective.domain.models.DialogueMessage
import com.example.mrdetective.domain.models.Note
import com.example.mrdetective.domain.repositories.NotebookRepository
import com.example.mrdetective.domain.models.DomainError
import javax.inject.Inject

class LoadNotebookUseCase @Inject constructor(
    private val repository: NotebookRepository
) {
    suspend operator fun invoke(
        investigationId: Int
    ): Result<Note> {
        if (investigationId <= 0) {
            return Result.failure(
                DomainError.Validation("Некорректный ID расследования")
            )
        }

        return repository.getNote(investigationId)
    }
}