package com.example.mrdetective.domain.repositories

import com.example.mrdetective.domain.models.Note

interface NotebookRepository {
    suspend fun getNote(investigationId: Int): Result<Note>

    suspend fun saveNote(
        investigationId: Int,
        text: String,
    ): Result<Note>

    suspend fun deleteNote(noteId: Int): Result<Unit>
}
