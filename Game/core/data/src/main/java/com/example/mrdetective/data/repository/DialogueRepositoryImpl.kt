package com.example.mrdetective.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.example.mrdetective.data.local.AppDatabase
import com.example.mrdetective.data.local.dao.*
import com.example.mrdetective.data.local.entity.DialogueMessageEntity
import com.example.mrdetective.data.local.entity.SyncQueueEntity
import com.example.mrdetective.data.local.toDomain
import com.example.mrdetective.data.network.ConnectivityChecker
import com.example.mrdetective.data.remote.api.OfflineApi
import com.example.mrdetective.data.remote.dto.NpcChatRequestDto
import com.example.mrdetective.data.sync.SyncScheduler
import com.example.mrdetective.domain.models.DialogueMessage
import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.repositories.DialogueRepository
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DialogueRepositoryImpl @Inject constructor(
    private val api: OfflineApi,
    private val progress: ProgressDao,
    private val dialogue: DialogueDao,
    private val outbox: SyncQueueDao,
    private val db: AppDatabase,
    private val connectivity: ConnectivityChecker,
    private val gson: Gson,
    @ApplicationContext private val context: Context,
) : DialogueRepository {
    override suspend fun getHistory(caseId: Int, suspectId: Int): Result<List<DialogueMessage>> = try {
        val cached = dialogue.getHistory(caseId, suspectId)
        val investigation = progress.getByCase(caseId)
        if (connectivity.isNetworkAvailable() && investigation != null) {
            val remote = api.getDialogue("eq.${investigation.id}", "eq.$suspectId")
            remote.forEach { row ->
                val id = (row["id"] as? Number)?.toLong() ?: return@forEach
                dialogue.insert(DialogueMessageEntity(
                    id = id, investigationId = investigation.id, caseId = caseId, suspectId = suspectId,
                    author = row["author"]?.toString() ?: "player", text = row["text"]?.toString() ?: "",
                    createdAt = row["create_at"]?.toString() ?: "", synced = true,
                ))
            }
        }
        Result.success(dialogue.getHistory(caseId, suspectId).map { it.toDomain() }.ifEmpty { cached.map { it.toDomain() } })
    } catch (error: Throwable) { Result.failure(error) }

    override suspend fun sendMessage(caseId: Int, suspectId: Int, message: String): Result<DialogueMessage> =
        send(caseId, suspectId, message, null)

    override suspend fun presentClue(caseId: Int, suspectId: Int, clueId: Int): Result<DialogueMessage> =
        send(caseId, suspectId, "Предъявляю улику №$clueId", clueId)

    private suspend fun send(caseId: Int, suspectId: Int, text: String, clueId: Int?): Result<DialogueMessage> = try {
        require(text.isNotBlank() && text.length <= 1000)
        val investigation = progress.getByCase(caseId) ?: throw DomainError.NotFound("Сначала начните расследование")
        val request = NpcChatRequestDto(investigation.id, suspectId, text, clueId)
        if (connectivity.isNetworkAvailable()) {
            try {
                val reply = api.npcChat(request)
                val now = System.currentTimeMillis().toString()
                val player = DialogueMessageEntity(-reply.messageId.toLong(), investigation.id, caseId, suspectId, "player", text, now, true)
                val suspect = DialogueMessageEntity(reply.messageId.toLong(), investigation.id, caseId, suspectId, "suspect", reply.reply, now, true)
                db.withTransaction { dialogue.insert(player); dialogue.insert(suspect) }
                Result.success(suspect.toDomain())
            } catch (networkError: Exception) {
                savePending(investigation.id, caseId, suspectId, text, request)
            }
        } else savePending(investigation.id, caseId, suspectId, text, request)
    } catch (error: Throwable) { Result.failure(error) }

    private suspend fun savePending(investigationId: Int, caseId: Int, suspectId: Int, text: String, request: NpcChatRequestDto): Result<DialogueMessage> {
        val localId = -System.nanoTime().coerceAtLeast(1L)
        val message = DialogueMessageEntity(localId, investigationId, caseId, suspectId, "player", text, System.currentTimeMillis().toString(), synced = false)
        db.withTransaction {
            dialogue.insert(message)
            outbox.enqueue(SyncQueueEntity(
                entityType = "dialogue_message", entityId = localId.toString(),
                operation = "send", payload = gson.toJson(request),
            ))
        }
        SyncScheduler.enqueue(context)
        return Result.success(message.toDomain())
    }
}
