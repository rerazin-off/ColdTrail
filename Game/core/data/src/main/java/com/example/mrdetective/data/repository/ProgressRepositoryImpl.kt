package com.example.mrdetective.data.repository

import android.content.Context
import androidx.room3.withWriteTransaction
import com.example.mrdetective.data.local.AppDatabase
import com.example.mrdetective.data.local.dao.InvestigationDao
import com.example.mrdetective.data.local.dao.SyncQueueDao
import com.example.mrdetective.data.local.entity.SyncQueueEntity
import com.example.mrdetective.data.sync.SyncScheduler
import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.models.InvestigationProgress
import com.example.mrdetective.domain.repositories.AuthRepository
import com.example.mrdetective.domain.repositories.ProgressRepository
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Хранит прогресс в Room и планирует синхронизацию с Supabase. */
@Singleton
class ProgressRepositoryImpl
    @Inject
    constructor(
        private val db: AppDatabase,
        private val auth: AuthRepository,
        private val progress: InvestigationDao,
        private val outbox: SyncQueueDao,
        private val gson: Gson,
        @ApplicationContext private val context: Context,
    ) : ProgressRepository {
        /** Восстанавливает сохранённый прогресс игрока по указанному делу. */
        override suspend fun getProgress(caseId: Int): Result<InvestigationProgress> {
            return try {
                val userId = auth.currentUser() ?: return Result.failure(DomainError.Unauthorized())
                val investigation =
                    progress.getByCaseForUser(caseId, userId)
                        ?: return Result.failure(DomainError.NotFound("Расследование ещё не начато"))
                Result.success(
                    InvestigationProgress(
                        id = investigation.id,
                        userId = investigation.userId,
                        caseId = caseId,
                        collectedClueIds = progress.collectedClueIds(investigation.id),
                        status = investigation.status,
                    ),
                )
            } catch (error: Throwable) {
                Result.failure(error)
            }
        }

        /** Отслеживает изменения списка идентификаторов улик, найденных в расследовании. */
        override fun observeCollectedClueIds(caseId: Int) =
            flow {
                val userId = auth.currentUser()
                if (userId == null) {
                    emit(emptySet())
                } else {
                    emitAll(progress.observeCollectedClueIds(caseId, userId).map { it.toSet() })
                }
            }

        /** Сохраняет переданный прогресс расследования. */
        override suspend fun saveProgress(progressModel: InvestigationProgress): Result<Unit> =
            try {
                db.withWriteTransaction {
                    progressModel.collectedClueIds.forEach { clueId ->
                        progress.collectClue(
                            com.example.mrdetective.data.local.entity.CollectedClueEntity(
                                progressModel.id,
                                clueId,
                                System.currentTimeMillis().toString(),
                                synced = false,
                            ),
                        )
                        outbox.enqueue(
                            SyncQueueEntity(
                                entityType = "collected_clue",
                                entityId = "${progressModel.id}:$clueId",
                                operation = "insert",
                                payload = gson.toJson(mapOf("investigation_id" to progressModel.id, "clue_id" to clueId)),
                            ),
                        )
                    }
                }
                SyncScheduler.enqueue(context)
                Result.success(Unit)
            } catch (error: Throwable) {
                Result.failure(error)
            }

        /** Записывает итоговое состояние расследования. */
        override suspend fun completeCase(caseId: Int): Result<Unit> {
            return try {
                val current =
                    progress.getByCase(caseId)
                        ?: return Result.failure(DomainError.NotFound("Расследование не найдено"))
                progress.upsertInvestigation(current.copy(status = "solved", updatedAt = System.currentTimeMillis()))
                Result.success(Unit)
            } catch (error: Throwable) {
                Result.failure(error)
            }
        }

        /** Запрашивает синхронизацию локально сохранённых изменений. */
        override suspend fun syncProgress(): Result<Unit> =
            try {
                SyncScheduler.enqueue(context)
                Result.success(Unit)
            } catch (error: Throwable) {
                Result.failure(error)
            }
    }
