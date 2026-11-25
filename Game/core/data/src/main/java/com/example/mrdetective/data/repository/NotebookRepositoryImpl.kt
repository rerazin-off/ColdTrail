package com.example.mrdetective.data.repository

import com.example.mrdetective.data.mapper.toDomain
import com.example.mrdetective.data.network.apiCall
import com.example.mrdetective.data.remote.api.SupabaseRestApi
import com.example.mrdetective.data.remote.dto.NoteInsertDto
import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.models.Note
import com.example.mrdetective.domain.repositories.NotebookRepository
import javax.inject.Inject
import javax.inject.Singleton

/** Reads and changes investigation notes directly through the Supabase REST API. */
@Singleton
class NotebookRepositoryImpl
    @Inject
    constructor(private val api: SupabaseRestApi) : NotebookRepository {
        /** Returns the latest note for an investigation, or a not-found error if there is none. */
        override suspend fun getNote(investigationId: Int): Result<Note> =
            apiCall {
                api.getLatestNote("eq.$investigationId").firstOrNull()?.toDomain()
                    ?: throw DomainError.NotFound("Заметка не найдена")
            }

        /** Stores a note and returns the saved note record. */
        override suspend fun saveNote(
            investigationId: Int,
            text: String,
        ): Result<Note> =
            apiCall {
                api.insertNote(NoteInsertDto(investigationId, text)).firstOrNull()?.toDomain()
                    ?: throw DomainError.Unknown(IllegalStateException("Supabase did not return inserted note"))
            }

        /** Removes a note by its identifier. */
        override suspend fun deleteNote(noteId: Int): Result<Unit> = apiCall { api.deleteNote("eq.$noteId") }
    }
