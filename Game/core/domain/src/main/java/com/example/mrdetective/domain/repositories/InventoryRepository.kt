package com.example.mrdetective.domain.repositories

import com.example.mrdetective.domain.models.Clue
import com.example.mrdetective.domain.models.CollectedClue

/** Предоставляет инвентарь расследования и регистрирует новые предметы. */
interface InventoryRepository {
    /** Возвращает записи об уликах в инвентаре расследования. */
    suspend fun getInventory(investigationId: Int): Result<List<Clue>>

    /** Добавляет указанную улику в инвентарь расследования. */
    suspend fun addItem(
        investigationId: Int,
        clueId: Int,
    ): Result<CollectedClue>
}
