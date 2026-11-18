package com.example.mrdetective.domain.usecases.dialogue

import com.example.mrdetective.domain.models.DialogueMessage
import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.repositories.DialogueRepository
import javax.inject.Inject

class SendSuspectMessageUseCase
    @Inject
    constructor(
        private val repository: DialogueRepository,
    ) {
        suspend operator fun invoke(
            caseId: Int,
            suspectId: Int,
            message: String,
        ): Result<DialogueMessage> {
            if (message.isBlank()) {
                return Result.failure(
                    DomainError.Validation("Сообщение не может быть пустым"),
                )
            }

            if (message.length > 1000) {
                return Result.failure(
                    DomainError.Validation("Сообщение слишком длинное"),
                )
            }

            return repository.sendMessage(
                caseId,
                suspectId,
                message,
            )
        }
    }
