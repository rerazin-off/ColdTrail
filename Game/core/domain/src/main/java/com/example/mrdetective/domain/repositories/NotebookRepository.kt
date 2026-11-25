package com.example.mrdetective.domain.repositories

import com.example.mrdetective.domain.models.Note

/** Provides the current note and its persistence operations. */
interface NotebookRepository {
    /** Returns the existing note for an investigation, if present. */
    suspend fun getNote(investigationId: Int): Result<Note>

    /** Stores a note and returns the saved note record. */
    suspend fun saveNote(
        investigationId: Int,
        text: String,
    ): Result<Note>

    /** Removes a note by its identifier. */
    suspend fun deleteNote(noteId: Int): Result<Unit>
}
