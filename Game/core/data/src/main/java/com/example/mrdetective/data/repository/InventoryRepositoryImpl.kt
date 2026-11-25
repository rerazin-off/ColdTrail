package com.example.mrdetective.data.repository

import android.content.Context
import androidx.room3.withWriteTransaction
import com.example.mrdetective.data.local.AppDatabase
import com.example.mrdetective.data.local.dao.ClueDao
import com.example.mrdetective.data.local.dao.InvestigationDao
import com.example.mrdetective.data.local.dao.SyncQueueDao
import com.example.mrdetective.data.local.entity.CollectedClueEntity
import com.example.mrdetective.data.local.entity.SyncQueueEntity
import com.example.mrdetective.data.local.toDomain
import com.example.mrdetective.data.sync.SyncScheduler
import com.example.mrdetective.domain.models.Clue
import com.example.mrdetective.domain.models.CollectedClue
import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.repositories.InventoryRepository
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Reads collected clues from the investigation inventory and records new entries. */
@Singleton
class InventoryRepositoryImpl
    @Inject
    constructor(
        private val clues: ClueDao,
        private val progress: InvestigationDao,
        private val db: AppDatabase,
        private val outbox: SyncQueueDao,
        private val gson: Gson,
        @ApplicationContext private val context: Context,
    ) : InventoryRepository {
        /** Returns the clue entries in an investigation inventory. */
        override suspend fun getInventory(investigationId: Int): Result<List<Clue>> =
            try {
                Result.success(clues.getInventory(investigationId).map { it.toDomain() })
            } catch (error: Throwable) {
                Result.failure(error)
            }

        /** Adds the specified clue to the investigation inventory. */
        override suspend fun addItem(
            investigationId: Int,
            clueId: Int,
        ): Result<CollectedClue> =
            try {
                progress.getInvestigation(investigationId)
                    ?: throw DomainError.NotFound("Расследование не найдено")
                if (clues.getById(clueId) == null) throw DomainError.NotFound("Предмет не найден в загруженном деле")
                val collectedAt = System.currentTimeMillis().toString()
                db.withWriteTransaction {
                    val inserted = progress.collectClue(CollectedClueEntity(investigationId, clueId, collectedAt, synced = false))
                    if (inserted != -1L) {
                        outbox.enqueue(
                            SyncQueueEntity(
                                entityType = "collected_clue",
                                entityId = "$investigationId:$clueId",
                                operation = "insert",
                                payload = gson.toJson(mapOf("investigation_id" to investigationId, "clue_id" to clueId)),
                            ),
                        )
                    }
                }
                SyncScheduler.enqueue(context)
                Result.success(CollectedClue(investigationId, clueId, collectedAt))
            } catch (error: Throwable) {
                Result.failure(error)
            }
    }
