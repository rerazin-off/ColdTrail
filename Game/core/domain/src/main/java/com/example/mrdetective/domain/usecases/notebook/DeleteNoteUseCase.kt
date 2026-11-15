package com.example.mrdetective.domain.usecases.notebook

import com.example.mrdetective.domain.repositories.NotebookRepository
import com.example.mrdetective.domain.models.DomainError
import javax.inject.Inject

class DeleteNoteUseCase @Inject constructor(
    private val repository: NotebookRepository
) {
    suspend operator fun invoke(
        noteId: Int
    ): Result<Unit> {
        if (noteId <= 0) {
            return Result.failure(
                DomainError.Validation("Некорректный ID записки")
            )
        }

        return repository.deleteNote(noteId)
    }
}