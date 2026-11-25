package com.example.mrdetective.domain.usecases.inventory

import com.example.mrdetective.domain.models.Clue
import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.repositories.ClueRepository
import com.example.mrdetective.domain.repositories.InventoryRepository
import javax.inject.Inject

/** Retrieves the description of an inventory clue. */
class InspectItemUseCase
    @Inject
    constructor(
        private val clueRepository: ClueRepository,
        private val inventoryRepository: InventoryRepository,
    ) {
        /** Returns the clue's metadata by its identifier. */
        suspend operator fun invoke(itemId: Int): Result<Clue> {
            if (itemId <= 0) {
                return Result.failure(
                    DomainError.Validation("Некорректный ID предмета"),
                )
            }

            val clueResult = clueRepository.getClue(itemId)

            if (clueResult.isFailure) {
                return Result.failure(
                    clueResult.exceptionOrNull()!!,
                )
            }

            return clueResult
        }
    }
