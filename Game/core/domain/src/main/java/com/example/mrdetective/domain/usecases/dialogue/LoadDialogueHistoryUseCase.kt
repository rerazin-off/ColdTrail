package com.example.mrdetective.domain.usecases.dialogue

import com.example.mrdetective.domain.models.DialogueMessage
import com.example.mrdetective.domain.repositories.DialogueRepository
import javax.inject.Inject

/** Loads the previous messages exchanged with a suspect. */
class LoadDialogueHistoryUseCase
    @Inject
    constructor(
        private val repository: DialogueRepository,
    ) {
        /** Returns the recorded dialogue for the selected suspect. */
        suspend operator fun invoke(
            caseId: Int,
            suspectId: Int,
        ): Result<List<DialogueMessage>> {
            return repository.getHistory(caseId, suspectId)
        }
    }
