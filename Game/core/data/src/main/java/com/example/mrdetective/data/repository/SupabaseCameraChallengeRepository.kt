package com.example.mrdetective.data.repository

import android.content.Context
import androidx.room3.withWriteTransaction
import com.example.mrdetective.data.local.AppDatabase
import com.example.mrdetective.data.local.entity.ClueEntity
import com.example.mrdetective.data.local.entity.PuzzleEntity
import com.example.mrdetective.data.local.entity.SyncQueueEntity
import com.example.mrdetective.data.local.toEntity
import com.example.mrdetective.data.network.ConnectivityChecker
import com.example.mrdetective.data.network.SupabaseConfig
import com.example.mrdetective.data.network.apiCall
import com.example.mrdetective.data.network.remoteOrCache
import com.example.mrdetective.data.remote.api.OfflineApi
import com.example.mrdetective.data.remote.dto.CameraCaptureDto
import com.example.mrdetective.data.sync.SyncScheduler
import com.example.mrdetective.domain.models.CameraChallenge
import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.repositories.AuthRepository
import com.example.mrdetective.domain.repositories.CameraChallengeRepository
import com.example.mrdetective.domain.repositories.CameraSubmission
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.flow.map
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

class SupabaseCameraChallengeRepository @Inject constructor(
    private val auth: AuthRepository,
    private val db: AppDatabase,
    private val api: OfflineApi,
    private val connectivity: ConnectivityChecker,
    private val gson: Gson,
    @param:ApplicationContext private val context: Context,
) : CameraChallengeRepository {
    override suspend fun load(caseId: Int): Result<List<CameraChallenge>> = apiCall {
        val user = auth.currentUser() ?: throw DomainError.Unauthorized()
        val investigation = db.investigationDao().getByCaseForUser(caseId, user)
            ?: throw DomainError.NotFound("Сначала начните расследование.")
        val id = investigation.id
        if (id <= 0) throw DomainError.Validation("Дождитесь синхронизации расследования.")
        val queued = db.syncQueueDao().pending()
            .filter { it.entityType == "camera_capture" }
            .mapTo(hashSetOf()) { it.entityId }

        remoteOrCache(
            connectivity,
            cached = {
                val clues = db.clueDao().getByCase(caseId).associateBy { it.id }
                val solved = db.investigationDao().solvedPuzzleIds(id).toSet()
                val collected = db.investigationDao().collectedClueIds(id).toSet()
                db.puzzleDao().getByCase(caseId)
                    .filter { it.kind == "camera" && (investigation.status == "in_progress" || it.id in solved) }
                    .mapNotNull { puzzle ->
                        val clue = clues[puzzle.rewardClueId] ?: return@mapNotNull null
                        puzzle.toChallenge(id, clue, puzzle.id in solved || "$id:${puzzle.id}" in queued, clue.id in collected)
                    }.ifEmpty { null }
            },
            remote = {
                val rows = api.getCameraChallenges(mapOf("p_investigation" to id))
                val clues = if (rows.isEmpty()) {
                    emptyMap()
                } else {
                    val freshClues = api.getCluesByCase("eq.$caseId").map { it.toEntity() }
                    db.clueDao().upsertAll(freshClues)
                    freshClues.associateBy { it.id }
                }
                val puzzles = rows.map { PuzzleEntity(it.id, caseId, "camera", it.title, it.hint, it.rewardClueId) }
                if (puzzles.isNotEmpty()) db.puzzleDao().upsertAll(puzzles)
                puzzles.zip(rows).mapNotNull { (puzzle, row) ->
                    val clue = clues[puzzle.rewardClueId] ?: return@mapNotNull null
                    puzzle.toChallenge(id, clue, row.solved || "$id:${puzzle.id}" in queued, row.rewardClaimed)
                }
            },
        ).getOrThrow()
    }

    override suspend fun submit(challenge: CameraChallenge, photoPath: String): Result<Unit> = apiCall {
        val owner = auth.currentUser() ?: throw DomainError.Unauthorized()
        val investigation = db.investigationDao().getInvestigation(challenge.investigationId)
        if (investigation?.userId != owner || investigation.status != "in_progress") {
            throw DomainError.Validation("Расследование уже недоступно.")
        }
        val file = File(photoPath)
        if (!file.isFile || file.length() !in 1..1_048_576L) {
            throw DomainError.Validation("Не удалось сохранить снимок.")
        }
        val request = CameraCaptureDto(challenge.investigationId, challenge.id, challenge.clueId, photoPath)
        db.syncQueueDao().enqueue(SyncQueueEntity(
            entityType = "camera_capture",
            entityId = challenge.queueKey,
            operation = "upload",
            payload = gson.toJson(request),
        ))
        SyncScheduler.enqueue(context)
    }

    override fun observeSubmission(challenge: CameraChallenge) =
        db.syncQueueDao().observeCameraCapture(challenge.investigationId, challenge.id, challenge.queueKey).map { status ->
            when (status) {
                2 -> CameraSubmission.Awarded
                1 -> CameraSubmission.Pending
                else -> CameraSubmission.Failed
            }
        }

    private fun PuzzleEntity.toChallenge(
        investigationId: Int,
        clue: ClueEntity,
        pendingOrCompleted: Boolean,
        rewardClaimed: Boolean,
    ) = CameraChallenge(
        id, investigationId, title, hint, clue.id, clue.title,
        clue.image.toClueImageUrl(), pendingOrCompleted, rewardClaimed,
    )

    private fun String?.toClueImageUrl(): String? {
        val path = this?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        val base = SupabaseConfig.baseUrl.toHttpUrlOrNull() ?: return null
        val prefix = "storage/v1/object/public/clue-images/"
        val absolute = path.toHttpUrlOrNull()
        if (absolute != null) {
            return path.takeIf { absolute.host == base.host && absolute.encodedPath.startsWith("/$prefix") }
        }
        return base.newBuilder()
            .addPathSegments(prefix + path.trimStart('/').removePrefix("clue-images/"))
            .build().toString()
    }

    private val CameraChallenge.queueKey get() = "$investigationId:$id"
}
