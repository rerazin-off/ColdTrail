package com.example.mrdetective.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.example.mrdetective.data.local.AppDatabase
import com.example.mrdetective.data.local.dao.*
import com.example.mrdetective.data.local.entity.*
import com.example.mrdetective.data.local.toDomain
import com.example.mrdetective.data.network.ConnectivityChecker
import com.example.mrdetective.data.remote.api.OfflineApi
import com.example.mrdetective.data.remote.dto.CheckPuzzleDto
import com.example.mrdetective.data.sync.SyncScheduler
import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.models.Puzzle
import com.example.mrdetective.domain.repositories.PuzzleRepository
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PuzzleRepositoryImpl @Inject constructor(
    private val api: OfflineApi,
    private val puzzles: PuzzleDao,
    private val progress: ProgressDao,
    private val outbox: SyncQueueDao,
    private val db: AppDatabase,
    private val connectivity: ConnectivityChecker,
    private val gson: Gson,
    @ApplicationContext private val context: Context,
) : PuzzleRepository {
    override suspend fun getPuzzle(id: Int): Result<Puzzle> = try {
        var local = puzzles.getById(id)
        if (local == null && connectivity.isNetworkAvailable()) {
            val dto = api.getPuzzle("eq.$id").firstOrNull() ?: throw DomainError.NotFound("Головоломка не найдена")
            local = PuzzleEntity(dto.id, dto.caseId, dto.kind, dto.title, dto.hint, dto.rewardClueId)
            puzzles.upsertAll(listOf(local))
        }
        local?.let { Result.success(it.toDomain()) } ?: Result.failure(DomainError.NotFound("Головоломка не сохранена на устройстве"))
    } catch (error: Throwable) { Result.failure(error) }

    override suspend fun markSolved(id: Int): Result<Unit> = try {
        val puzzle = puzzles.getById(id) ?: throw DomainError.NotFound("Головоломка не найдена")
        val investigation = progress.getByCase(puzzle.caseId) ?: throw DomainError.NotFound("Расследование не начато")
        db.withTransaction {
            progress.markPuzzleSolved(SolvedPuzzleEntity(investigation.id, id, System.currentTimeMillis().toString(), synced = false))
        }
        Result.success(Unit)
    } catch (error: Throwable) { Result.failure(error) }

    override suspend fun checkAnswer(investigationId: Int, puzzleId: Int, answer: String): Result<Boolean> = try {
        require(answer.isNotBlank()) { "Ответ не может быть пустым" }
        val request = CheckPuzzleDto(investigationId, puzzleId, answer)
        if (connectivity.isNetworkAvailable()) {
            val correct = api.checkPuzzle(request)
            if (correct) {
                val puzzle = puzzles.getById(puzzleId)
                db.withTransaction {
                    progress.markPuzzleSolved(SolvedPuzzleEntity(investigationId, puzzleId, System.currentTimeMillis().toString(), synced = true))
                    puzzle?.rewardClueId?.let { progress.collectClue(CollectedClueEntity(investigationId, it, System.currentTimeMillis().toString(), synced = true)) }
                }
            }
            Result.success(correct)
        } else {
            // Keep the submitted answer locally; server remains the only authority that can validate it.
            outbox.enqueue(SyncQueueEntity(
                entityType = "puzzle_answer", entityId = "$investigationId:$puzzleId",
                operation = "check", payload = gson.toJson(request),
            ))
            SyncScheduler.enqueue(context)
            Result.failure(DomainError.Network(IllegalStateException("Ответ сохранён и ожидает серверной проверки после восстановления сети.")))
        }
    } catch (error: Throwable) { Result.failure(error) }
}
