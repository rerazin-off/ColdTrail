package com.example.mrdetective.domain.usecases.dialogue

import com.example.mrdetective.domain.models.DialogueMessage
import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.repositories.DialogueRepository
import javax.inject.Inject

/** Presents inventory evidence to a suspect and requests a response. */
class PresentClueToSuspectUseCase
    @Inject
    constructor(
        private val repository: DialogueRepository,
    ) {
        /** Sends the selected evidence to the suspect through the dialogue repository. */
        suspend operator fun invoke(
            caseId: Int,
            suspectId: Int,
            clueId: Int,
            message: String = "Предъявляю улику №$clueId",
        ): Result<DialogueMessage> {
            if (caseId <= 0 || suspectId <= 0 || clueId <= 0) {
                return Result.failure(
                    DomainError.Validation("Некорректные идентификаторы"),
                )
            }
            if (message.isBlank() || message.length > 1000) {
                return Result.failure(DomainError.Validation("Некорректный текст сообщения"))
            }

            return repository.presentClue(
                caseId,
                suspectId,
                clueId,
                message,
            )
        }
    }
