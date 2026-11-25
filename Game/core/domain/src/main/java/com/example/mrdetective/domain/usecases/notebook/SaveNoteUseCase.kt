package com.example.mrdetective.domain.usecases.notebook

import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.models.Note
import com.example.mrdetective.domain.repositories.NotebookRepository
import javax.inject.Inject

/** Проверяет и сохраняет заметку игрока по расследованию. */
class SaveNoteUseCase
    @Inject
    constructor(
        private val repository: NotebookRepository,
    ) {
        /** Отклоняет пустой текст перед сохранением заметки. */
        suspend operator fun invoke(
            investigationId: Int,
            text: String,
        ): Result<Note> {
            if (investigationId <= 0 || text.isBlank()) {
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
