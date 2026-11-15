package com.example.mrdetective.domain.usecases.dialogue

import com.example.mrdetective.domain.models.DialogueMessage
import com.example.mrdetective.domain.repositories.DialogueRepository
import javax.inject.Inject

class LoadDialogueHistoryUseCase @Inject constructor(
    private val repository: DialogueRepository
) {
    suspend operator fun invoke(
        caseId: Int,
        suspectId: Int
    ): Result<List<DialogueMessage>> {
        return repository.getHistory(caseId, suspectId)
    }
}