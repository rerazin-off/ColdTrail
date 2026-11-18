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

@Singleton
class SupabaseNotebookRepositoryImpl @Inject constructor(private val api: SupabaseRestApi) : NotebookRepository {
    // SQL allows multiple notes per investigation; Domain currently asks for a single note.
    override suspend fun getNote(investigationId: Int): Result<Note> = apiCall {
        api.getLatestNote("eq.$investigationId").firstOrNull()?.toDomain()
            ?: throw DomainError.NotFound("Заметка не найдена")
    }
    override suspend fun saveNote(investigationId: Int, text: String): Result<Note> = apiCall {
        api.insertNote(NoteInsertDto(investigationId, text)).firstOrNull()?.toDomain()
            ?: throw DomainError.Unknown(IllegalStateException("Supabase did not return inserted note"))
    }
    override suspend fun deleteNote(noteId: Int): Result<Unit> = apiCall { api.deleteNote("eq.$noteId") }
}
