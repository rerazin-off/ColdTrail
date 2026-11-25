package com.example.mrdetective.domain.usecases.inventory

import com.example.mrdetective.domain.models.Clue
import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.repositories.InventoryRepository
import javax.inject.Inject

/** Загружает улики, собранные в расследовании. */
class LoadInventoryUseCase
    @Inject
    constructor(
        private val repository: InventoryRepository,
    ) {
        /** Возвращает сохранённые улики расследования. */
        suspend operator fun invoke(investigationId: Int): Result<List<Clue>> {
            if (investigationId == 0) {
                return Result.failure(
                    DomainError.Validation("Некорректный ID расследования"),
                )
            }

            return repository.getInventory(investigationId)
        }
    }
