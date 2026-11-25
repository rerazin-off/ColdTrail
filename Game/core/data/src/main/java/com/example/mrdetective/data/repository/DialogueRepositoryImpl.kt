package com.example.mrdetective.data.repository

import android.content.Context
import androidx.room3.withWriteTransaction
import com.example.mrdetective.data.local.AppDatabase
import com.example.mrdetective.data.local.dao.DialogueDao
import com.example.mrdetective.data.local.dao.InvestigationDao
import com.example.mrdetective.data.local.dao.SyncQueueDao
import com.example.mrdetective.data.local.entity.DialogueMessageEntity
import com.example.mrdetective.data.local.entity.SyncQueueEntity
import com.example.mrdetective.data.local.toDomain
import com.example.mrdetective.data.network.ConnectivityChecker
import com.example.mrdetective.data.network.NetworkErrorHandler
import com.example.mrdetective.data.remote.api.OfflineApi
import com.example.mrdetective.data.remote.dto.NpcChatRequestDto
import com.example.mrdetective.data.sync.SyncScheduler
import com.example.mrdetective.domain.models.DialogueMessage
import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.repositories.AuthRepository
import com.example.mrdetective.domain.repositories.DialogueRepository
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

/**
 * Читает историю из кеша и отправляет новые вопросы в серверную функцию диалога.
 *
 * @param api Retrofit API операций расследования, головоломок и синхронизации.
 * @param auth Источник активной сессии и идентификатора игрока.
 * @param progress DAO текущих расследований и записей прогресса.
 * @param dialogue DAO для сообщений и истории диалога.
 * @param outbox DAO очереди локальных изменений, ожидающих отправки.
 * @param db Локальная база Room.
 * @param connectivity Проверка доступности сети перед обращением к серверу.
 * @param gson Сериализатор JSON для сетевых данных и записей очереди.
 * @param context Контекст Android для доступа к ресурсам и системным службам.
 */
@Singleton
class DialogueRepositoryImpl
    @Inject
    constructor(
        private val api: OfflineApi,
        private val auth: AuthRepository,
        private val progress: InvestigationDao,
        private val dialogue: DialogueDao,
        private val outbox: SyncQueueDao,
        private val db: AppDatabase,
        private val connectivity: ConnectivityChecker,
        private val gson: Gson,
        @ApplicationContext private val context: Context,
    ) : DialogueRepository {
        /** Возвращает историю диалога для указанного дела и подозреваемого. */
        override suspend fun getHistory(
            caseId: Int,
            suspectId: Int,
        ): Result<List<DialogueMessage>> =
            try {
                val userId = auth.currentUser() ?: throw DomainError.Unauthorized()
                val investigation =
                    progress.getByCaseForUser(caseId, userId)
                        ?: throw DomainError.NotFound("Сначала начните расследование")
                val cached = dialogue.getInvestigationHistory(investigation.id, suspectId)
                val connected = connectivity.isNetworkAvailable()
                if (!connected && cached.isEmpty()) throw DomainError.Network(kind = DomainError.Network.Kind.Offline)
                if (investigation.id > 0 && connected) {
                    try {
                        val remote =
                            api.getDialogue("eq.${investigation.id}", "eq.$suspectId").mapNotNull { row ->
                                val id = (row["id"] as? Number)?.toLong() ?: return@mapNotNull null
                                DialogueMessageEntity(
                                    id = id,
                                    investigationId = investigation.id,
                                    caseId = caseId,
                                    suspectId = suspectId,
                                    author = row["author"]?.toString() ?: "player",
                                    text = row["text"]?.toString().orEmpty(),
                                    createdAt = row["create_at"]?.toString().orEmpty(),
                                    synced = true,
                                )
                            }
                        db.withWriteTransaction {
                            remote.forEach { dialogue.insert(it) }
                            val replyIds = remote.filter { it.author == "suspect" }.mapTo(mutableSetOf()) { it.id }
                            val playerTexts = remote.filter { it.author == "player" }.mapTo(mutableSetOf()) { it.text }
                            val provisional =
                                cached.filter {
                                    it.id < 0 && -it.id in replyIds && it.author == "player" && it.text in playerTexts
                                }.map { it.id }
                            if (provisional.isNotEmpty()) {
                                dialogue.removeProvisionalMessages(investigation.id, suspectId, provisional)
                            }
                        }
                    } catch (error: CancellationException) {
                        throw error
                    } catch (error: Exception) {
                        if (cached.isEmpty()) throw error
                    }
                }
                Result.success(dialogue.getInvestigationHistory(investigation.id, suspectId).map { it.toDomain() })
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Result.failure(NetworkErrorHandler.map(error))
            }

        /** Отправляет вопрос игрока и возвращает ответ подозреваемого. */
        override suspend fun sendMessage(
            caseId: Int,
            suspectId: Int,
            message: String,
        ): Result<DialogueMessage> = send(caseId, suspectId, message, null)

        /** Предъявляет подозреваемому улику из инвентаря и возвращает его реакцию. */
        override suspend fun presentClue(
            caseId: Int,
            suspectId: Int,
            clueId: Int,
            message: String,
        ): Result<DialogueMessage> = send(caseId, suspectId, message, clueId)

        private suspend fun send(
            caseId: Int,
            suspectId: Int,
            text: String,
            clueId: Int?,
        ): Result<DialogueMessage> =
            try {
                require(text.isNotBlank() && text.length <= 1000)
                val userId = auth.currentUser() ?: throw DomainError.Unauthorized()
                val investigation = progress.getByCaseForUser(caseId, userId) ?: throw DomainError.NotFound("Сначала начните расследование")
                val request = NpcChatRequestDto(investigation.id, suspectId, text, clueId)
                if (investigation.id > 0 && connectivity.isNetworkAvailable()) {
                    try {
                        val reply = api.npcChat(request)
                        val now = System.currentTimeMillis().toString()
                        val player =
                            DialogueMessageEntity(-reply.messageId.toLong(), investigation.id, caseId, suspectId, "player", text, now, true)
                        val suspect =
                            DialogueMessageEntity(
                                reply.messageId.toLong(),
                                investigation.id,
                                caseId,
                                suspectId,
                                "suspect",
                                reply.reply,
                                now,
                                true,
                            )
                        db.withWriteTransaction {
                            dialogue.insert(player)
                            dialogue.insert(suspect)
                        }
                        Result.success(suspect.toDomain())
                    } catch (error: CancellationException) {
                        throw error
                    } catch (networkError: Exception) {
                        val failure = NetworkErrorHandler.map(networkError)
                        if (failure is DomainError.Network) {
                            savePending(investigation.id, caseId, suspectId, text, request)
                        } else {
                            Result.failure(failure)
                        }
                    }
                } else {
                    savePending(investigation.id, caseId, suspectId, text, request)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Result.failure(NetworkErrorHandler.map(error))
            }

        private suspend fun savePending(
            investigationId: Int,
            caseId: Int,
            suspectId: Int,
            text: String,
            request: NpcChatRequestDto,
        ): Result<DialogueMessage> {
            val localId = -System.nanoTime().coerceAtLeast(1L)
            val message =
                DialogueMessageEntity(
                    localId,
                    investigationId,
                    caseId,
                    suspectId,
                    "player",
                    text,
                    System.currentTimeMillis().toString(),
                    synced = false,
                )
            db.withWriteTransaction {
                dialogue.insert(message)
                outbox.enqueue(
                    SyncQueueEntity(
                        entityType = "dialogue_message",
                        entityId = localId.toString(),
                        operation = "send",
                        payload = gson.toJson(request),
                    ),
                )
            }
            SyncScheduler.enqueue(context)
            return Result.success(message.toDomain())
        }
    }
