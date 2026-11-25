package com.example.mrdetective.domain.usecases.inventory

import com.example.mrdetective.domain.models.Clue
import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.repositories.InventoryRepository
import javax.inject.Inject

/**
 * Загружает улики, собранные в расследовании.
 *
 * @param repository Репозиторий предметов и улик расследования.
 */
class LoadInventoryUseCase
    @Inject
    constructor(
        private val repository: InventoryRepository,
    ) {
        /**
         * Возвращает сохранённые улики расследования.
         *
         * @param investigationId Ненулевой идентификатор расследования. Отрицательный идентификатор относится
         *   к локальной записи.
         * @return Улики расследования, в том числе пустой список, или ошибка загрузки.
         */
        suspend operator fun invoke(investigationId: Int): Result<List<Clue>> {
            if (investigationId == 0) {
                return Result.failure(
                    DomainError.Validation("Некорректный ID расследования"),
                )
            }

            return repository.getInventory(investigationId)
        }
    }
