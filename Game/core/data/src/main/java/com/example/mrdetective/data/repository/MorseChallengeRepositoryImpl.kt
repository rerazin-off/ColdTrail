package com.example.mrdetective.data.repository

import androidx.room3.withWriteTransaction
import com.example.mrdetective.data.local.AppDatabase
import com.example.mrdetective.data.local.entity.CollectedClueEntity
import com.example.mrdetective.data.local.entity.MorseSignalEntity
import com.example.mrdetective.data.local.entity.PuzzleEntity
import com.example.mrdetective.data.local.entity.SolvedPuzzleEntity
import com.example.mrdetective.data.local.toEntity
import com.example.mrdetective.data.network.ConnectivityChecker
import com.example.mrdetective.data.network.apiCall
import com.example.mrdetective.data.network.remoteOrCache
import com.example.mrdetective.data.remote.api.OfflineApi
import com.example.mrdetective.data.remote.dto.MorseChallengeDto
import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.models.MorseChallenge
import com.example.mrdetective.domain.models.MorseChallenges
import com.example.mrdetective.domain.repositories.AuthRepository
import com.example.mrdetective.domain.repositories.MorseChallengeRepository
import javax.inject.Inject

/**
 * Загружает головоломки Морзе с сервера или из локального кеша.
 *
 * @param auth Источник активной сессии и идентификатора игрока.
 * @param connectivity Проверка доступности сети перед обращением к серверу.
 * @param api Retrofit API операций расследования, головоломок и синхронизации.
 * @param db Локальная база Room.
 */
class MorseChallengeRepositoryImpl
    @Inject
    constructor(
        private val auth: AuthRepository,
        private val connectivity: ConnectivityChecker,
        private val api: OfflineApi,
        private val db: AppDatabase,
    ) : MorseChallengeRepository {
        /** Возвращает сигнал, статус решения и награды для головоломок текущего расследования. */
        override suspend fun load(caseId: Int): Result<MorseChallenges> =
            apiCall {
                val userId = auth.currentUser() ?: throw DomainError.Unauthorized()
                val progress = db.investigationDao()
                val investigation =
                    progress.getByCaseForUser(caseId, userId)
                        ?: throw DomainError.NotFound("Сначала начните расследование.")
                val id = investigation.id
                val puzzles = db.puzzleDao()

                suspend fun cached(): MorseChallenges? {
                    val signals = puzzles.getMorseSignals(caseId).associateBy { it.puzzleId }
                    if (signals.isEmpty()) return null
                    val solved = progress.solvedPuzzleIds(id).toSet()
                    val claimed = progress.collectedClueIds(id).toSet()
                    val options =
                        puzzles.getByCase(caseId).mapNotNull { puzzle ->
                            if (puzzle.kind != "morse" || (investigation.status != "in_progress" && puzzle.id !in solved)) {
                                return@mapNotNull null
                            }
                            val signal = signals[puzzle.id]?.signal ?: return@mapNotNull null
                            val reward = puzzle.rewardClueId ?: return@mapNotNull null
                            MorseChallenge(puzzle.id, puzzle.title, puzzle.hint, signal, reward, puzzle.id in solved, reward in claimed)
                        }
                    return MorseChallenges(id, options)
                }

                if (id < 0) return@apiCall cached() ?: throw DomainError.Network(kind = DomainError.Network.Kind.Offline)
                remoteOrCache(
                    connectivity,
                    cached = ::cached,
                    remote = {
                        val rows = api.getMorseChallenges(mapOf("p_investigation" to id))
                        db.cacheMorseChallenges(caseId, id, rows)
                        val known = db.clueDao().getByCase(caseId).mapTo(hashSetOf()) { it.id }
                        if (rows.any { it.rewardClueId !in known }) {
                            db.clueDao().upsertAll(api.getCluesByCase("eq.$caseId").map { it.toEntity() })
                        }
                        MorseChallenges(
                            id,
                            rows.map { MorseChallenge(it.id, it.title, it.hint, it.signal, it.rewardClueId, it.solved, it.rewardClaimed) },
                        )
                    },
                ).getOrThrow()
            }
    }

/** Сохраняет сигналы Морзе и подтверждённые результаты одной транзакцией. */
internal suspend fun AppDatabase.cacheMorseChallenges(
    caseId: Int,
    investigationId: Int,
    rows: List<MorseChallengeDto>,
) {
    if (rows.isEmpty()) return
    val now = System.currentTimeMillis().toString()
    withWriteTransaction {
        puzzleDao().upsertAll(rows.map { PuzzleEntity(it.id, caseId, "morse", it.title, it.hint, it.rewardClueId) })
        puzzleDao().saveMorseSignals(rows.map { MorseSignalEntity(it.id, caseId, it.signal) })
        rows.filter { it.solved }.forEach {
            investigationDao().markPuzzleSolved(SolvedPuzzleEntity(investigationId, it.id, now, synced = true))
        }
        rows.filter { it.rewardClaimed }.forEach {
            investigationDao().collectClue(CollectedClueEntity(investigationId, it.rewardClueId, now, synced = true))
        }
    }
}
