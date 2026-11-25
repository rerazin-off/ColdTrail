package com.example.mrdetective.data.repository

import androidx.room3.withWriteTransaction
import com.example.mrdetective.data.local.AppDatabase
import com.example.mrdetective.data.local.dao.ClueDao
import com.example.mrdetective.data.local.dao.InvestigationDao
import com.example.mrdetective.data.local.dao.PuzzleDao
import com.example.mrdetective.data.local.entity.CollectedClueEntity
import com.example.mrdetective.data.local.entity.PuzzleEntity
import com.example.mrdetective.data.local.entity.SolvedPuzzleEntity
import com.example.mrdetective.data.local.toEntity
import com.example.mrdetective.data.network.apiCall
import com.example.mrdetective.data.remote.api.OfflineApi
import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.models.MorseChallenge
import com.example.mrdetective.domain.models.MorseChallenges
import com.example.mrdetective.domain.repositories.AuthRepository
import com.example.mrdetective.domain.repositories.MorseChallengeRepository
import javax.inject.Inject

/** Объединяет головоломки Морзе с сервера и сохранённые на устройстве результаты. */
class MorseChallengeRepositoryImpl
    @Inject
    constructor(
        private val auth: AuthRepository,
        private val progress: InvestigationDao,
        private val puzzles: PuzzleDao,
        private val clues: ClueDao,
        private val api: OfflineApi,
        private val db: AppDatabase,
    ) : MorseChallengeRepository {
        /** Возвращает головоломки Морзе указанного дела и расследования. */
        override suspend fun load(caseId: Int): Result<MorseChallenges> =
            apiCall {
                val userId = auth.currentUser() ?: throw DomainError.Unauthorized()
                val investigation =
                    progress.getByCaseForUser(caseId, userId)
                        ?: throw DomainError.NotFound("Сначала начните расследование.")
                if (investigation.id <= 0) {
                    throw DomainError.Validation("Дождитесь синхронизации расследования с сервером.")
                }
                val rows = api.getMorseChallenges(mapOf("p_investigation" to investigation.id))
                if (rows.isNotEmpty()) {
                    val knownClueIds = clues.getByCase(caseId).mapTo(hashSetOf()) { it.id }
                    if (rows.any { it.rewardClueId !in knownClueIds }) {
                        clues.upsertAll(api.getCluesByCase("eq.$caseId").map { it.toEntity() })
                    }
                    val now = System.currentTimeMillis().toString()
                    db.withWriteTransaction {
                        puzzles.upsertAll(
                            rows.map {
                                PuzzleEntity(it.id, caseId, "morse", it.title, it.hint, it.rewardClueId)
                            },
                        )
                        rows.filter { it.solved }.forEach {
                            progress.markPuzzleSolved(SolvedPuzzleEntity(investigation.id, it.id, now, synced = true))
                        }
                        rows.filter { it.rewardClaimed }.forEach {
                            progress.collectClue(CollectedClueEntity(investigation.id, it.rewardClueId, now, synced = true))
                        }
                    }
                }
                MorseChallenges(
                    investigation.id,
                    rows.map { MorseChallenge(it.id, it.title, it.hint, it.signal, it.rewardClueId, it.solved, it.rewardClaimed) },
                )
            }
    }
