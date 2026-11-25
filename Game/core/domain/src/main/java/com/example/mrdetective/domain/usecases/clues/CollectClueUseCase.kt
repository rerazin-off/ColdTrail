package com.example.mrdetective.domain.usecases.clues

import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.repositories.ClueRepository
import javax.inject.Inject

/** Records a clue collected during an investigation. */
class CollectClueUseCase
    @Inject
    constructor(
        private val repository: ClueRepository,
    ) {
        /** Records the selected clue for the given investigation. */
        suspend operator fun invoke(clueId: Int): Result<Unit> {
            if (clueId <= 0) {
                return Result.failure(
                    DomainError.Validation("Некорректный ID улики"),
                )
            }

            return repository.collectClue(clueId)
        }
    }
