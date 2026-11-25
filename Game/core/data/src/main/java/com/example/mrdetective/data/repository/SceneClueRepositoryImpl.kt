package com.example.mrdetective.data.repository

import android.content.Context
import androidx.room3.withWriteTransaction
import com.example.mrdetective.data.local.AppDatabase
import com.example.mrdetective.data.local.entity.CollectedClueEntity
import com.example.mrdetective.data.local.entity.SolvedPuzzleEntity
import com.example.mrdetective.data.local.entity.SyncQueueEntity
import com.example.mrdetective.data.network.ConnectivityChecker
import com.example.mrdetective.data.network.apiCall
import com.example.mrdetective.data.network.toClueImageUrl
import com.example.mrdetective.data.remote.api.OfflineApi
import com.example.mrdetective.data.remote.dto.CameraCaptureDto
import com.example.mrdetective.data.sync.SyncScheduler
import com.example.mrdetective.domain.models.CameraChallenge
import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.repositories.AuthRepository
import com.example.mrdetective.domain.repositories.SceneClueImage
import com.example.mrdetective.domain.repositories.SceneClueRepository
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Загружает изображения для поиска на сцене и отправляет выбранную улику.
 *
 * @param db Локальная база Room.
 * @param api Retrofit API операций расследования, головоломок и синхронизации.
 * @param connectivity Проверка доступности сети перед обращением к серверу.
 * @param auth Источник активной сессии и идентификатора игрока.
 * @param gson Сериализатор JSON для сетевых данных и записей очереди.
 * @param context Контекст Android для доступа к ресурсам и системным службам.
 */
class SceneClueRepositoryImpl
    @Inject
    constructor(
        private val db: AppDatabase,
        private val api: OfflineApi,
        private val connectivity: ConnectivityChecker,
        private val auth: AuthRepository,
        private val gson: Gson,
        @param:ApplicationContext private val context: Context,
    ) : SceneClueRepository {
        /** Загружает изображения других улик, исключая искомую улику. */
        override suspend fun loadDecoys(targetClueId: Int): List<SceneClueImage> {
            val remote =
                if (connectivity.isNetworkAvailable()) {
                    apiCall { api.getClueImages("neq.$targetClueId") }.getOrNull().orEmpty()
                        .mapNotNull { row -> row.image.toClueImageUrl()?.let { SceneClueImage(row.id, it, row.title) } }
                } else {
                    emptyList()
                }
            val cached =
                apiCall { db.clueDao().getClueImages() }.getOrNull().orEmpty().mapNotNull { row ->
                    row.image.toClueImageUrl()?.let { SceneClueImage(row.id, it, row.title) }
                }
            return remote + cached
        }

        /** Отправляет искомую улику, найденную на запасной сцене. */
        override suspend fun submit(
            challenge: CameraChallenge,
            selectedClueId: Int,
        ): Result<Unit> =
            apiCall {
                val userId = auth.currentUser() ?: throw DomainError.Unauthorized()
                val investigation = db.investigationDao().getInvestigation(challenge.investigationId)
                if (investigation?.userId != userId || investigation.status != "in_progress") {
                    throw DomainError.Validation("Расследование уже недоступно.")
                }
                if (!connectivity.isNetworkAvailable()) {
                    if (selectedClueId != challenge.clueId) throw DomainError.Validation("Не удалось подтвердить улику.")
                    val request = CameraCaptureDto(challenge.investigationId, challenge.id, selectedClueId, "")
                    db.syncQueueDao().enqueue(
                        SyncQueueEntity(
                            entityType = "camera_capture",
                            entityId = "${challenge.investigationId}:${challenge.id}",
                            operation = "scene",
                            payload = gson.toJson(request),
                        ),
                    )
                    SyncScheduler.enqueue(context)
                    throw DomainError.SyncPending()
                }
                val reward =
                    api.completeSceneCameraPuzzle(
                        mapOf(
                            "p_investigation" to challenge.investigationId,
                            "p_puzzle" to challenge.id,
                            "p_selected_clue" to selectedClueId,
                        ),
                    )
                if (reward != challenge.clueId) throw DomainError.Validation("Не удалось подтвердить улику.")
                val now = System.currentTimeMillis().toString()
                val progress = db.investigationDao()
                db.withWriteTransaction {
                    progress.markPuzzleSolved(SolvedPuzzleEntity(challenge.investigationId, challenge.id, now, synced = true))
                    progress.collectClue(CollectedClueEntity(challenge.investigationId, reward, now, synced = true))
                }
            }
    }
