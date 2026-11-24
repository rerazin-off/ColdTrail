package com.example.mrdetective.data.repository

import android.content.Context
import com.example.mrdetective.data.local.AppDatabase
import com.example.mrdetective.data.local.dao.CaseDao
import com.example.mrdetective.data.local.dao.ClueDao
import com.example.mrdetective.data.local.dao.InvestigationDao
import com.example.mrdetective.data.local.dao.PuzzleDao
import com.example.mrdetective.data.local.dao.SuspectDao
import com.example.mrdetective.data.local.dao.SyncQueueDao
import com.example.mrdetective.data.local.entity.InvestigationEntity
import com.example.mrdetective.data.local.entity.PuzzleEntity
import com.example.mrdetective.data.local.toEntity
import com.example.mrdetective.data.mapper.toDomain
import androidx.room3.withWriteTransaction
import com.example.mrdetective.data.local.toDomain
import com.example.mrdetective.data.network.ConnectivityChecker
import com.example.mrdetective.data.network.remoteOrCache
import com.example.mrdetective.data.network.apiCall
import com.example.mrdetective.data.remote.api.OfflineApi
import com.example.mrdetective.data.remote.api.SupabaseRestApi
import com.example.mrdetective.data.remote.dto.InvestigationInsertDto
import com.example.mrdetective.data.sync.SyncScheduler
import com.example.mrdetective.domain.models.Case
import com.example.mrdetective.domain.models.Clue
import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.repositories.AuthRepository
import com.example.mrdetective.domain.repositories.CaseRepository
import com.example.mrdetective.domain.repositories.ClueRepository
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CaseRepositoryImpl @Inject constructor(
    private val db: AppDatabase,
    private val api: SupabaseRestApi,
    private val offlineApi: OfflineApi,
    private val local: CaseDao,
    private val suspects: SuspectDao,
    private val clues: ClueDao,
    private val puzzles: PuzzleDao,
    private val progress: InvestigationDao,
    private val outbox: SyncQueueDao,
    private val auth: AuthRepository,
    private val connectivity: ConnectivityChecker,
    private val gson: Gson,
    @ApplicationContext private val context: Context,
) : CaseRepository {
    init { SyncScheduler.schedulePeriodic(context); SyncScheduler.enqueue(context) }

    override suspend fun getAvailableCases(): Result<List<Case>> = remoteOrCache(
        connectivity,
        cached = { local.getAll().map { it.toDomain() }.takeIf { it.isNotEmpty() } },
        remote = { api.getCases().also { local.upsertAll(it.map { dto -> dto.toEntity() }) }.map { it.toDomain() } },
    ).fold(onSuccess = { cases ->
        apiCall {
            val userId = auth.currentUser() ?: return@apiCall cases
            val investigations = progress.getForUser(userId).distinctBy { it.caseId }.associateBy { it.caseId }
            cases.map { case ->
                investigations[case.id]?.let { case.copy(status = it.status) } ?: case
            }
        }
    }, onFailure = { Result.failure(it) })

    override suspend fun getCase(id: Int): Result<Case> = remoteOrCache(
        connectivity,
        cached = {
            local.getById(id)?.toDomain()?.copy(
                clueIds = clues.getByCase(id).mapTo(mutableSetOf()) { it.id }.takeIf { it.isNotEmpty() },
            )
        },
        remote = {
            val dto = api.getCase("eq.$id").firstOrNull() ?: throw DomainError.NotFound("Дело не найдено")
            // First open downloads all reference data needed to play this case offline.
            local.upsert(dto.toEntity())
            val suspectRows = api.getSuspects("eq.$id")
            val clueRows = offlineApi.getCluesByCase("eq.$id")
            val puzzleRows = offlineApi.getPuzzlesByCase("eq.$id")
            suspects.upsertAll(suspectRows.map { it.toEntity() })
            clues.upsertAll(clueRows.map { it.toEntity() })
            puzzles.upsertAll(puzzleRows.map {
                PuzzleEntity(
                    it.id,
                    it.caseId,
                    it.kind,
                    it.title,
                    it.hint,
                    it.rewardClueId
                )
            })
            dto.toDomain().copy(clueIds = clueRows.mapTo(mutableSetOf()) { it.id })
        },
    )

    override suspend fun startCase(id: Int): Result<Unit> {
        return try {
            val userId = auth.currentUser() ?: return Result.failure(DomainError.Unauthorized())
            if (progress.getByCaseForUser(id, userId) != null) return Result.success(Unit)
            if (connectivity.isNetworkAvailable()) {
                try {
                    val created = offlineApi.startInvestigation(InvestigationInsertDto(id)).firstOrNull()
                        ?: offlineApi.getInvestigations("eq.$id", "eq.$userId").firstOrNull()
                    if (created != null) {
                        progress.upsertInvestigation(
                            InvestigationEntity(
                                created.id,
                                created.userId,
                                created.caseId,
                                created.status,
                                created.startedAt
                            )
                        )
                        return Result.success(Unit)
                    }
                } catch (_: Exception) { /* fall through to local-first start */ }
            }
            val localId = -((System.nanoTime() % Int.MAX_VALUE).toInt().coerceAtLeast(1))
            db.withWriteTransaction {
                progress.upsertInvestigation(InvestigationEntity(localId, userId, id, "in_progress", System.currentTimeMillis().toString()))
                outbox.enqueue(com.example.mrdetective.data.local.entity.SyncQueueEntity(
                    entityType = "investigation", entityId = localId.toString(), operation = "insert",
                    payload = gson.toJson(InvestigationInsertDto(id)),
                ))
            }
            SyncScheduler.enqueue(context)
            Result.success(Unit)
        } catch (error: Throwable) {
            Result.failure(error)
        }
    }
}

