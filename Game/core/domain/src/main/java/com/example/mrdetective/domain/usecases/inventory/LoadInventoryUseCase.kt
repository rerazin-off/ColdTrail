package com.example.mrdetective.domain.usecases.inventory

import com.example.mrdetective.domain.models.Clue
import com.example.mrdetective.domain.repositories.InventoryRepository
import com.example.mrdetective.domain.models.DomainError
import javax.inject.Inject

class LoadInventoryUseCase @Inject constructor(
    private val repository: InventoryRepository
) {
    suspend operator fun invoke(
        caseId: Int
    ): Result<List<Clue>> {
        if (caseId <= 0) {
            return Result.failure(
                DomainError.Validation("Некорректный ID дела")
            )
        }

        return repository.getInventory(caseId)
    }
}