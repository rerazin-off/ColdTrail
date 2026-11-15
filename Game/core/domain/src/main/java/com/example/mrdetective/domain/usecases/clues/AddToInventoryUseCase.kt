package com.example.mrdetective.domain.usecases.clues

import com.example.mrdetective.domain.models.Clue
import com.example.mrdetective.domain.models.CollectedClue
import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.models.InvestigationProgress
import com.example.mrdetective.domain.repositories.InventoryRepository
import javax.inject.Inject

class AddToInventoryUseCase
    @Inject
    constructor(
        private val repository: InventoryRepository,
    ) {
        suspend operator fun invoke(
            item: Clue,
            investigation: InvestigationProgress,
        ): Result<CollectedClue> {
            if (item.id <= 0) {
                return Result.failure(
                    DomainError.Validation("Некорректная улика"),
                )
            }

            return repository.addItem(investigation.id, item.id)
        }
    }
