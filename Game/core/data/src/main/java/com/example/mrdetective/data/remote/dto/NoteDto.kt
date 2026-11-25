package com.example.mrdetective.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Запись заметки из REST API Supabase.
 *
 * @property id Идентификатор заметки.
 * @property investigationId Идентификатор расследования. Для локально начатого дела до синхронизации он может
 *   быть отрицательным.
 * @property text Текст заметки из ответа Supabase.
 * @property createdAt Время создания записи.
 * @property updatedAt Время последнего обновления записи.
 */
data class NoteDto(
    val id: Int,
    @SerializedName("investigation_id") val investigationId: Int,
    val text: String,
    @SerializedName("create_at") val createdAt: String,
    @SerializedName("update_at") val updatedAt: String,
)

/**
 * Поля для создания или обновления заметки расследования.
 *
 * @property investigationId Идентификатор расследования. Для локально начатого дела до синхронизации он может
 *   быть отрицательным.
 * @property text Текст новой заметки.
 * @property clientId Ключ повторяемости запроса, по которому сервер распознаёт одну и ту же заметку.
 */
data class NoteInsertDto(
    @SerializedName("investigation_id") val investigationId: Int,
    val text: String,
    @SerializedName("client_id") val clientId: String? = null,
)
