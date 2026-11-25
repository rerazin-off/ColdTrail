package com.example.mrdetective.data.repository

import android.content.Context
import androidx.room3.withWriteTransaction
import com.example.mrdetective.data.local.AppDatabase
import com.example.mrdetective.data.local.entity.NoteEntity
import com.example.mrdetective.data.local.entity.SyncQueueEntity
import com.example.mrdetective.data.local.toDomain
import com.example.mrdetective.data.local.toEntity
import com.example.mrdetective.data.network.ConnectivityChecker
import com.example.mrdetective.data.network.apiCall
import com.example.mrdetective.data.network.remoteOrCache
import com.example.mrdetective.data.remote.api.SupabaseRestApi
import com.example.mrdetective.data.sync.SyncScheduler
import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.models.Note
import com.example.mrdetective.domain.repositories.AuthRepository
import com.example.mrdetective.domain.repositories.NotebookRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Хранит заметки в Room и синхронизирует новые записи через очередь WorkManager.
 *
 * @param db Локальная база Room.
 * @param api Retrofit API чтения и изменения таблиц Supabase.
 * @param auth Источник активной сессии и идентификатора игрока.
 * @param connectivity Проверка доступности сети перед обращением к серверу.
 * @param context Контекст Android для доступа к ресурсам и системным службам.
 */
@Singleton
class NotebookRepositoryImpl
    @Inject
    constructor(
        private val db: AppDatabase,
        private val api: SupabaseRestApi,
        private val auth: AuthRepository,
        private val connectivity: ConnectivityChecker,
        @param:ApplicationContext private val context: Context,
    ) : NotebookRepository {
        /** Возвращает последнюю заметку, включая ещё не отправленные на сервер записи. */
        override suspend fun getNote(investigationId: Int): Result<Note> =
            apiCall {
                requireOwner(investigationId)
                val notes = db.noteDao()
                val cached = notes.latest(investigationId)
                if (cached?.synced == false || investigationId < 0) {
                    return@apiCall cached?.toDomain() ?: throw DomainError.NotFound("Заметка не найдена")
                }
                remoteOrCache(
                    connectivity,
                    cached = { notes.latest(investigationId)?.toDomain() },
                    remote = {
                        val remote = api.getLatestNote("eq.$investigationId").firstOrNull()
                        if (remote != null) notes.upsert(remote.toEntity())
                        notes.latest(investigationId)?.toDomain() ?: throw DomainError.NotFound("Заметка не найдена")
                    },
                ).getOrThrow()
            }

        /** Сохраняет заметку вместе с заданием на синхронизацию в одной транзакции. */
        override suspend fun saveNote(
            investigationId: Int,
            text: String,
        ): Result<Note> =
            apiCall {
                val note =
                    db.withWriteTransaction {
                        requireOwner(investigationId)
                        val notes = db.noteDao()
                        val id = (notes.minLocalId() ?: 0) - 1
                        val now = Instant.now().toString()
                        val saved = NoteEntity(id, investigationId, text, now, now, synced = false)
                        notes.upsert(saved)
                        db.syncQueueDao().enqueue(
                            SyncQueueEntity(
                                entityType = "note_create",
                                entityId = id.toString(),
                                operation = "insert",
                                payload = UUID.randomUUID().toString(),
                            ),
                        )
                        saved
                    }
                SyncScheduler.enqueue(context)
                note.toDomain()
            }

        /** Удаляет серверную заметку и её локальную копию. */
        override suspend fun deleteNote(noteId: Int): Result<Unit> =
            apiCall {
                api.deleteNote("eq.$noteId")
                db.noteDao().delete(noteId)
            }

        private suspend fun requireOwner(investigationId: Int) {
            val userId = auth.currentUser() ?: throw DomainError.Unauthorized()
            if (db.investigationDao().getInvestigation(investigationId)?.userId != userId) {
                throw DomainError.NotFound("Расследование не найдено")
            }
        }
    }
