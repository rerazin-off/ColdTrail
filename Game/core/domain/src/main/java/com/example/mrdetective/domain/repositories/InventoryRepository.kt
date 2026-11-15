package com.example.mrdetective.domain.repositories

import com.example.mrdetective.domain.models.Clue
import com.example.mrdetective.domain.models.CollectedClue

interface InventoryRepository {
    suspend fun getInventory(investigationId: Int): Result<List<Clue>>

    suspend fun addItem(
        investigationId: Int,
        clueId: Int,
    ): Result<CollectedClue>
}
