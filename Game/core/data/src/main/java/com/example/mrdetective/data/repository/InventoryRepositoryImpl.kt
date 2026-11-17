package com.example.mrdetective.data.repository

import android.content.Context
import com.example.mrdetective.data.sync.SyncScheduler
import dagger.hilt.android.qualifiers.ApplicationContext

import com.example.mrdetective.data.local.LocalGameStore
import com.example.mrdetective.data.local.dao.ClueDao
import com.example.mrdetective.data.local.dao.ProgressDao
import com.example.mrdetective.data.local.toDomain
import com.example.mrdetective.domain.models.Clue
import com.example.mrdetective.domain.models.CollectedClue
import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.repositories.InventoryRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InventoryRepositoryImpl @Inject constructor(
    private val clues: ClueDao,
    private val progress: ProgressDao,
    private val localGame: LocalGameStore,
    @ApplicationContext private val context: Context,
) : InventoryRepository {
    override suspend fun getInventory(investigationId: Int): Result<List<Clue>> = try {
        Result.success(clues.getInventory(investigationId).map { it.toDomain() })
    } catch (error: Throwable) { Result.failure(error) }

    override suspend fun addItem(investigationId: Int, clueId: Int): Result<CollectedClue> = try {
        val investigation = progress.getInvestigation(investigationId)
            ?: throw DomainError.NotFound("Расследование не найдено")
        if (clues.getById(clueId) == null) throw DomainError.NotFound("Предмет не найден в загруженном деле")
        localGame.collectClue(investigationId, clueId)
        SyncScheduler.enqueue(context)
        Result.success(CollectedClue(investigationId, clueId, System.currentTimeMillis().toString()))
    } catch (error: Throwable) { Result.failure(error) }
}
