package com.example.mrdetective.domain.usecases.dialogue

import com.example.mrdetective.domain.models.DialogueMessage
import com.example.mrdetective.domain.repositories.DialogueRepository
import com.example.mrdetective.domain.models.DomainError
import javax.inject.Inject

class PresentClueToSuspectUseCase @Inject constructor(
    private val repository: DialogueRepository
) {
    suspend operator fun invoke(
        caseId: Int,
        suspectId: Int,
        clueId: Int
    ): Result<DialogueMessage> {
        if (caseId <= 0 || suspectId <= 0 || clueId <= 0) {
            return Result.failure(
                DomainError.Validation("Некорректные идентификаторы")
            )
        }

        return repository.presentClue(
            caseId,
            suspectId,
            clueId
        )
    }
}