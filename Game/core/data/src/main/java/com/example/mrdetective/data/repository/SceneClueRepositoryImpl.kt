package com.example.mrdetective.data.repository

import androidx.room3.withWriteTransaction
import com.example.mrdetective.data.local.AppDatabase
import com.example.mrdetective.data.local.entity.CollectedClueEntity
import com.example.mrdetective.data.local.entity.SolvedPuzzleEntity
import com.example.mrdetective.data.network.ConnectivityChecker
import com.example.mrdetective.data.network.apiCall
import com.example.mrdetective.data.network.toClueImageUrl
import com.example.mrdetective.data.remote.api.OfflineApi
import com.example.mrdetective.domain.models.CameraChallenge
import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.repositories.SceneClueImage
import com.example.mrdetective.domain.repositories.SceneClueRepository
import javax.inject.Inject

/** Загружает изображения для поиска на сцене и отправляет выбранную улику. */
class SceneClueRepositoryImpl
    @Inject
    constructor(
        private val db: AppDatabase,
        private val api: OfflineApi,
        private val connectivity: ConnectivityChecker,
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
