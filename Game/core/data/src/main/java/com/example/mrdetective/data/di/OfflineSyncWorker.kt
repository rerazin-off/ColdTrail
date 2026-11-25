package com.example.mrdetective.data.sync

import android.content.Context
import androidx.room3.withWriteTransaction
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.mrdetective.data.local.AppDatabase
import com.example.mrdetective.data.local.entity.CollectedClueEntity
import com.example.mrdetective.data.local.entity.DialogueMessageEntity
import com.example.mrdetective.data.local.entity.NoteEntity
import com.example.mrdetective.data.local.entity.PuzzleEntity
import com.example.mrdetective.data.local.entity.SolvedPuzzleEntity
import com.example.mrdetective.data.local.toEntity
import com.example.mrdetective.data.network.NetworkErrorHandler
import com.example.mrdetective.data.network.SupabaseConfig
import com.example.mrdetective.data.network.apiCall
import com.example.mrdetective.data.remote.api.OfflineApi
import com.example.mrdetective.data.remote.api.SupabaseRestApi
import com.example.mrdetective.data.remote.dto.CameraCaptureDto
import com.example.mrdetective.data.remote.dto.CheckPuzzleDto
import com.example.mrdetective.data.remote.dto.InvestigationInsertDto
import com.example.mrdetective.data.remote.dto.NoteInsertDto
import com.example.mrdetective.data.remote.dto.NpcChatRequestDto
import com.example.mrdetective.data.repository.cacheMorseChallenges
import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.repositories.AuthRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import retrofit2.HttpException
import java.io.File
import kotlin.collections.map

/** Открывает фоновой задаче WorkManager доступ к зависимостям Hilt. */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface SyncWorkerEntryPoint {
    /** Возвращает локальную базу данных фоновой задаче. */
    fun database(): AppDatabase

    /** Возвращает фоновой задаче API расследований и синхронизации. */
    fun offlineApi(): OfflineApi

    /** Возвращает фоновой задаче REST API таблиц. */
    fun restApi(): SupabaseRestApi

    /** Возвращает фоновой задаче источник текущей сессии игрока. */
    fun auth(): AuthRepository
}

/**
 * WorkManager повторяет операции при временных сбоях и запускает задачу при наличии сетевого подключения.
 *
 * @param context Контекст Android для доступа к ресурсам и системным службам.
 * @param params Параметры запуска, которые передаёт WorkManager.
 */
class OfflineSyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    /** Отправляет доступные операции из очереди и ожидающие загрузки фото для активного аккаунта. */
    override suspend fun doWork(): Result =
        withContext(Dispatchers.IO) {
            val entry = EntryPointAccessors.fromApplication(applicationContext, SyncWorkerEntryPoint::class.java)
            val db = entry.database()
            val api = entry.offlineApi()
            val restApi = entry.restApi()
            try {
                val userId = entry.auth().currentUser() ?: return@withContext Result.success()
                val deferredOps = mutableSetOf<Long>()
                while (true) {
                    val op = db.syncQueueDao().pending().firstOrNull { it.id !in deferredOps } ?: break
                    try {
                        when (op.entityType) {
                            "note_create" -> {
                                val noteId = checkNotNull(op.entityId.toIntOrNull())
                                val local = db.noteDao().getById(noteId)
                                if (local != null) {
                                    val investigation = db.investigationDao().getInvestigation(local.investigationId)
                                    if (investigation?.userId != userId || local.investigationId <= 0) {
                                        deferredOps.add(op.id)
                                        continue
                                    }
                                    val saved = restApi.syncNote(NoteInsertDto(local.investigationId, local.text, op.payload)).first()
                                    db.withWriteTransaction {
                                        db.noteDao().upsert(saved.toEntity().copy(localUpdatedAt = local.localUpdatedAt))
                                        db.noteDao().delete(noteId)
                                        db.syncQueueDao().remove(op.id)
                                    }
                                    continue
                                }
                            }

                            "collected_clue" ->
                                api.postQueued(
                                    SupabaseConfig.baseUrl + "rest/v1/collected_clues",
                                    "resolution=ignore-duplicates,return=minimal",
                                    op.payload.toRequestBody("application/json".toMediaType()),
                                ).close()

                            "note" ->
                                when (op.operation) {
                                    "delete" -> api.deleteQueued(SupabaseConfig.baseUrl + "rest/v1/notes?id=eq.${op.entityId}").close()
                                    else -> {
                                        val response =
                                            api.postQueued(
                                                SupabaseConfig.baseUrl + "rest/v1/notes",
                                                "return=representation",
                                                op.payload.toRequestBody("application/json".toMediaType()),
                                            )
                                        val rows = JSONArray(response.string())
                                        if (rows.length() > 0 && op.entityId.toIntOrNull() != null) {
                                            db.noteDao().delete(op.entityId.toInt())
                                            val dto = rows.getJSONObject(0)
                                            db.noteDao().upsert(
                                                NoteEntity(
                                                    dto.getInt("id"),
                                                    dto.getInt("investigation_id"),
                                                    dto.getString("text"),
                                                    dto.getString("create_at"),
                                                    dto.getString("update_at"),
                                                    System.currentTimeMillis(),
                                                    true,
                                                ),
                                            )
                                        }
                                    }
                                }

                            "investigation" -> {
                                val localId = checkNotNull(op.entityId.toIntOrNull())
                                val local = db.investigationDao().getInvestigation(localId)
                                if (local != null) {
                                    if (local.userId != userId) {
                                        deferredOps.add(op.id)
                                        continue
                                    }
                                    val server =
                                        api.startInvestigation(InvestigationInsertDto(local.caseId)).firstOrNull()
                                            ?: api.getInvestigations("eq.${local.caseId}", "eq.${local.userId}").firstOrNull()
                                            ?: error("Сервер не вернул расследование")
                                    val serverId = server.id
                                    db.withWriteTransaction {
                                        db.investigationDao().remapInvestigation(localId, serverId)
                                        db.investigationDao().remapCollected(localId, serverId)
                                        db.investigationDao().remapSolved(localId, serverId)
                                        db.dialogueDao().remapInvestigation(localId, serverId)
                                        db.noteDao().remapInvestigation(localId, serverId)
                                        db.syncQueueDao().replacePayload("\"investigation_id\":$localId", "\"investigation_id\":$serverId")
                                        db.syncQueueDao().replacePayload("\"p_investigation\":$localId", "\"p_investigation\":$serverId")
                                    }
                                }
                            }

                            "dialogue_message" -> {
                                val request = GsonHolder.gson.fromJson(op.payload, NpcChatRequestDto::class.java)
                                val localId = checkNotNull(op.entityId.toLongOrNull())
                                val player = db.dialogueDao().getById(localId)
                                if (player != null) {
                                    val investigation = db.investigationDao().getInvestigation(player.investigationId)
                                    if (investigation?.userId != userId || player.investigationId <= 0) {
                                        deferredOps.add(op.id)
                                        continue
                                    }
                                    val reply = api.npcChat(request.copy(investigationId = player.investigationId))
                                    db.withWriteTransaction {
                                        db.dialogueDao().delete(localId)
                                        db.dialogueDao().insert(player.copy(id = -reply.messageId.toLong(), synced = true))
                                        db.dialogueDao().insert(
                                            player.copy(
                                                id = reply.messageId.toLong(),
                                                author = "suspect",
                                                text = reply.reply,
                                                synced = true,
                                            ),
                                        )
                                        db.syncQueueDao().remove(op.id)
                                    }
                                }
                            }

                            "puzzle_answer" -> {
                                val request = GsonHolder.gson.fromJson(op.payload, CheckPuzzleDto::class.java)
                                val investigation = db.investigationDao().getInvestigation(request.investigationId)
                                if (investigation?.userId != userId || request.investigationId <= 0) {
                                    deferredOps.add(op.id)
                                    continue
                                }
                                val correct = api.checkPuzzle(request)
                                if (correct) {
                                    db.investigationDao().markPuzzleSolved(
                                        SolvedPuzzleEntity(
                                            request.investigationId,
                                            request.puzzleId,
                                            System.currentTimeMillis().toString(),
                                            true,
                                        ),
                                    )
                                    val rewardId = db.puzzleDao().getById(request.puzzleId)?.rewardClueId
                                    if (rewardId != null) {
                                        db.investigationDao().collectClue(
                                            CollectedClueEntity(
                                                request.investigationId,
                                                rewardId,
                                                System.currentTimeMillis().toString(),
                                                synced = true,
                                            ),
                                        )
                                    }
                                }
                            }

                            "camera_capture" -> {
                                val request = GsonHolder.gson.fromJson(op.payload, CameraCaptureDto::class.java)
                                val investigation = db.investigationDao().getInvestigation(request.investigationId)
                                if (investigation?.userId != userId) {
                                    deferredOps.add(op.id)
                                    continue
                                }
                                val file = if (op.operation == "scene") null else File(request.filePath)
                                if (file != null && !file.isFile) {
                                    db.syncQueueDao().remove(op.id)
                                    continue
                                }
                                val parameters = mapOf("p_investigation" to request.investigationId, "p_puzzle" to request.puzzleId)
                                val reward =
                                    if (file == null) {
                                        api.completeSceneCameraPuzzle(parameters + ("p_selected_clue" to request.clueId))
                                    } else {
                                        api.uploadPhoto(
                                            "clue-photos",
                                            "$userId/${request.clueId}.jpg",
                                            file.readBytes().toRequestBody("image/jpeg".toMediaType()),
                                        ).close()
                                        api.completeCameraPuzzle(parameters)
                                    }
                                val now = System.currentTimeMillis().toString()
                                db.withWriteTransaction {
                                    db.investigationDao().markPuzzleSolved(
                                        SolvedPuzzleEntity(request.investigationId, request.puzzleId, now, synced = true),
                                    )
                                    db.investigationDao().collectClue(
                                        CollectedClueEntity(request.investigationId, reward, now, synced = true),
                                    )
                                    db.syncQueueDao().remove(op.id)
                                }
                                file?.delete()
                                continue
                            }

                            "accusation" ->
                                api.postQueued(
                                    SupabaseConfig.baseUrl + "functions/v1/submit-accusation",
                                    null,
                                    op.payload.toRequestBody("application/json".toMediaType()),
                                ).close()

                            else -> throw IllegalStateException("Неизвестная операция очереди: ${op.entityType}")
                        }
                        db.syncQueueDao().remove(op.id)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        if (e is HttpException && e.code() in 400..499 && e.code() !in listOf(408, 429)) {
                            // Ошибка данных не исправится повтором. Операция остаётся локально видимой, но снимается с автоповтора.
                            db.syncQueueDao().recordFailure(op.id, "HTTP ${e.code()}: ${e.message()}")
                            if (op.entityType == "note_create") {
                                deferredOps.add(op.id)
                                continue
                            }
                            if (op.entityType == "camera_capture" && op.operation != "scene") {
                                val request = GsonHolder.gson.fromJson(op.payload, CameraCaptureDto::class.java)
                                File(request.filePath).delete()
                            }
                            db.syncQueueDao().remove(op.id)
                        } else {
                            val message =
                                when ((NetworkErrorHandler.map(e) as? DomainError.Network)?.kind) {
                                    DomainError.Network.Kind.Server -> "Сервер временно недоступен. Попробуйте ещё раз позже."
                                    DomainError.Network.Kind.Offline -> "Нет подключения к интернету. Проверьте сеть и повторите."
                                    DomainError.Network.Kind.Timeout -> "Сервер не ответил вовремя. Попробуйте ещё раз."
                                    else -> "Не удалось отправить данные. Проверьте подключение и повторите."
                                }
                            db.syncQueueDao().recordFailure(op.id, message)
                            throw e
                        }
                    }
                }

                for (photo in db.pendingPhotoDao().pending()) {
                    try {
                        val file = File(photo.localPath)
                        if (!file.exists()) {
                            db.pendingPhotoDao().remove(photo.id)
                            continue
                        }

                        val extension = file.extension.lowercase().ifBlank { "webp" }
                        val mime =
                            when (extension) {
                                "jpg", "jpeg" -> "image/jpeg"
                                "png" -> "image/png"
                                else -> "image/webp"
                            }
                        val storagePath = "$userId/${photo.clueId}.$extension"
                        api.uploadPhoto("clue-photos", storagePath, file.readBytes().toRequestBody(mime.toMediaType())).close()
                        db.pendingPhotoDao().remove(photo.id)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        db.pendingPhotoDao().incrementAttempts(photo.id)
                        throw e
                    }
                }
                refreshLocalSnapshot(db, api, restApi, userId)
                Result.success()
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                Result.retry()
            }
        }

    private suspend fun refreshLocalSnapshot(
        db: AppDatabase,
        api: OfflineApi,
        restApi: SupabaseRestApi,
        userId: String,
    ) {
        val cases = restApi.getCases()
        db.caseDao().upsertAll(cases.map { it.toEntity() })
        for (case in cases) {
            val suspectDtos = restApi.getSuspects("eq.${case.id}")
            db.suspectDao().upsertAll(suspectDtos.map { it.toEntity() })
            db.clueDao().upsertAll(api.getCluesByCase("eq.${case.id}").map { it.toEntity() })
            val puzzles = api.getPuzzlesByCase("eq.${case.id}")
            db.puzzleDao().upsertAll(
                puzzles.map {
                    PuzzleEntity(
                        it.id,
                        it.caseId,
                        it.kind,
                        it.title,
                        it.hint,
                        it.rewardClueId,
                    )
                },
            )

            val investigation = db.investigationDao().getByCaseForUser(case.id, userId) ?: continue
            if (investigation.id <= 0) continue
            apiCall {
                db.cacheMorseChallenges(case.id, investigation.id, api.getMorseChallenges(mapOf("p_investigation" to investigation.id)))
            }
            api.getCollectedClues("eq.${investigation.id}").forEach { row ->
                val clueId = (row["clue_id"] as? Number)?.toInt() ?: return@forEach
                val collectedAt = row["collected_at"]?.toString() ?: System.currentTimeMillis().toString()
                db.investigationDao().collectClue(CollectedClueEntity(investigation.id, clueId, collectedAt, synced = true))
            }
            api.getSolvedPuzzles("eq.${investigation.id}").forEach { row ->
                val puzzleId = (row["puzzle_id"] as? Number)?.toInt() ?: return@forEach
                val solvedAt = row["solved_at"]?.toString() ?: System.currentTimeMillis().toString()
                db.investigationDao().markPuzzleSolved(SolvedPuzzleEntity(investigation.id, puzzleId, solvedAt, synced = true))
            }
            restApi.getLatestNote("eq.${investigation.id}").firstOrNull()?.let { db.noteDao().upsert(it.toEntity()) }
            for (suspect in suspectDtos) {
                api.getDialogue("eq.${investigation.id}", "eq.${suspect.id}").forEach { row ->
                    val messageId = (row["id"] as? Number)?.toLong() ?: return@forEach
                    db.dialogueDao().insert(
                        DialogueMessageEntity(
                            id = messageId,
                            investigationId = investigation.id,
                            caseId = case.id,
                            suspectId = suspect.id,
                            author = row["author"]?.toString() ?: "player",
                            text = row["text"]?.toString() ?: "",
                            createdAt = row["create_at"]?.toString() ?: "",
                            synced = true,
                        ),
                    )
                }
            }
        }
    }
}

/** Хранит сериализатор JSON для чтения операций из очереди синхронизации. */
private object GsonHolder {
    val gson = com.google.gson.Gson()
}
