package com.example.mrdetective.domain.usecases.inventory

import com.example.mrdetective.domain.models.Clue
import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.repositories.ClueRepository
import com.example.mrdetective.domain.repositories.InventoryRepository
import javax.inject.Inject

/** Загружает описание улики из инвентаря. */
class InspectItemUseCase
    @Inject
    constructor(
        private val clueRepository: ClueRepository,
        private val inventoryRepository: InventoryRepository,
    ) {
        /** Возвращает метаданные улики по идентификатору. */
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
