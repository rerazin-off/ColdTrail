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
import com.example.mrdetective.data.network.NetworkErrorHandler
import com.example.mrdetective.data.sync.SyncScheduler
import com.example.mrdetective.domain.models.Clue
import com.example.mrdetective.domain.models.CollectedClue
import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.repositories.InventoryRepository
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Читает найденные улики из инвентаря и добавляет новые записи.
 *
 * @param clues DAO кеша улик и инвентаря.
 * @param progress DAO текущих расследований и записей прогресса.
 * @param db Локальная база Room.
 * @param outbox DAO очереди локальных изменений, ожидающих отправки.
 * @param gson Сериализатор JSON для сетевых данных и записей очереди.
 * @param context Контекст Android для доступа к ресурсам и системным службам.
 */
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
        /** Возвращает записи об уликах в инвентаре расследования. */
        override suspend fun getInventory(investigationId: Int): Result<List<Clue>> =
            try {
                Result.success(clues.getInventory(investigationId).map { it.toDomain() })
            } catch (error: Throwable) {
                Result.failure(NetworkErrorHandler.map(error))
            }

        /** Добавляет указанную улику в инвентарь расследования. */
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
                Result.failure(NetworkErrorHandler.map(error))
            }
    }
