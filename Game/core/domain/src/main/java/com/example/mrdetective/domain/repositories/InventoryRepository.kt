package com.example.mrdetective.domain.repositories

import com.example.mrdetective.domain.models.Clue
import com.example.mrdetective.domain.models.CollectedClue

/** Provides the inventory of an investigation and records added items. */
interface InventoryRepository {
    /** Returns the clue entries in an investigation inventory. */
    suspend fun getInventory(investigationId: Int): Result<List<Clue>>

    /** Adds the specified clue to the investigation inventory. */
    suspend fun addItem(
        investigationId: Int,
        clueId: Int,
    ): Result<CollectedClue>
}
