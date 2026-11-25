package com.example.mrdetective.domain.repositories

import com.example.mrdetective.domain.models.Note

/** Предоставляет операции чтения, сохранения и удаления заметки. */
interface NotebookRepository {
    /** Возвращает заметку расследования, если она существует. */
    suspend fun getNote(investigationId: Int): Result<Note>

    /** Сохраняет заметку и возвращает сохранённую запись. */
    suspend fun saveNote(
        investigationId: Int,
        text: String,
    ): Result<Note>

    /** Удаляет заметку по идентификатору. */
    suspend fun deleteNote(noteId: Int): Result<Unit>
}
