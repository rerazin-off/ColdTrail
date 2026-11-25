package com.example.mrdetective.data.repository

import androidx.room3.withWriteTransaction
import com.example.mrdetective.data.local.AppDatabase
import com.example.mrdetective.data.local.entity.CollectedClueEntity
import com.example.mrdetective.data.local.entity.PuzzleEntity
import com.example.mrdetective.data.local.entity.SolvedPuzzleEntity
import com.example.mrdetective.data.local.toEntity
import com.example.mrdetective.data.network.ConnectivityChecker
import com.example.mrdetective.data.network.apiCall
import com.example.mrdetective.data.network.remoteOrCache
import com.example.mrdetective.data.remote.api.OfflineApi
import com.example.mrdetective.domain.models.CodeChallenge
import com.example.mrdetective.domain.models.CodeChallenges
import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.repositories.AuthRepository
import com.example.mrdetective.domain.repositories.CodeChallengeRepository
import javax.inject.Inject

/** Формирует числовые головоломки по их метаданным и сохранённому прогрессу. */
class CodeChallengeRepositoryImpl
    @Inject
    constructor(
        private val auth: AuthRepository,
        private val db: AppDatabase,
        private val api: OfflineApi,
        private val connectivity: ConnectivityChecker,
    ) : CodeChallengeRepository {
        /** Возвращает числовые головоломки указанного дела и расследования. */
        override suspend fun load(caseId: Int): Result<CodeChallenges> =
            apiCall {
                val owner = auth.currentUser() ?: throw DomainError.Unauthorized()
                val progress = db.investigationDao()
                val investigation =
                    progress.getByCaseForUser(caseId, owner)
                        ?: throw DomainError.NotFound("Сначала начните расследование.")
                val id = investigation.id
                if (id <= 0) throw DomainError.Validation("Дождитесь синхронизации расследования.")

                fun challenges(
                    puzzles: List<PuzzleEntity>,
                    clueIds: Set<Int>,
                    solved: Set<Int>,
                    claimed: Set<Int>,
                ) = CodeChallenges(
                    id,
                    puzzles.mapNotNull { puzzle ->
                        val reward = puzzle.rewardClueId ?: return@mapNotNull null
                        val isSolved = puzzle.id in solved
                        if (reward !in clueIds || (investigation.status != "in_progress" && !isSolved)) return@mapNotNull null
                        CodeChallenge(puzzle.id, puzzle.title, puzzle.hint, reward, isSolved, reward in claimed)
                    },
                )

                remoteOrCache(
                    connectivity,
                    cached = {
                        val puzzles = db.puzzleDao().getByCase(caseId).filter { it.kind == "code" }
                        if (puzzles.isEmpty()) {
                            null
                        } else {
                            challenges(
                                puzzles,
                                db.clueDao().getByCase(caseId).mapTo(hashSetOf()) { it.id },
                                progress.solvedPuzzleIds(id).toSet(),
                                progress.collectedClueIds(id).toSet(),
                            )
                        }
                    },
                    remote = {
                        val puzzles =
                            api.getPuzzlesByCase("eq.$caseId")
                                .filter { it.caseId == caseId && it.kind == "code" && it.rewardClueId != null }
                                .map { PuzzleEntity(it.id, it.caseId, it.kind, it.title, it.hint, it.rewardClueId) }
                        if (puzzles.isEmpty()) return@remoteOrCache CodeChallenges(id, emptyList())
                        val clueRows = api.getCluesByCase("eq.$caseId")
                        val solved = api.getSolvedPuzzles("eq.$id").mapNotNullTo(hashSetOf()) { (it["puzzle_id"] as? Number)?.toInt() }
                        val claimed = api.getCollectedClues("eq.$id").mapNotNullTo(hashSetOf()) { (it["clue_id"] as? Number)?.toInt() }
                        val now = System.currentTimeMillis().toString()
                        db.withWriteTransaction {
                            db.clueDao().upsertAll(clueRows.map { it.toEntity() })
                            db.puzzleDao().upsertAll(puzzles)
                            puzzles.filter { it.id in solved }.forEach {
                                progress.markPuzzleSolved(SolvedPuzzleEntity(id, it.id, now, synced = true))
                            }
                            clueRows.filter { it.id in claimed }.forEach {
                                progress.collectClue(CollectedClueEntity(id, it.id, now, synced = true))
                            }
                        }
                        challenges(
                            puzzles,
                            clueRows.mapTo(hashSetOf()) { it.id },
                            progress.solvedPuzzleIds(id).toSet(),
                            progress.collectedClueIds(id).toSet(),
                        )
                    },
                ).getOrThrow()
            }
    }
