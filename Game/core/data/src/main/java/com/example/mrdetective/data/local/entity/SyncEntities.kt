package com.example.mrdetective.data.local.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

/**
 * Запись очереди операции, ожидающей отправки в Supabase.
 *
 * @property id Идентификатор операции синхронизации.
 * @property entityType Тип записи, от которого зависит обработка операции в очереди.
 * @property entityId Идентификатор записи, связанной с операцией синхронизации.
 * @property operation Конкретное действие, которое выполнит обработчик очереди.
 * @property payload Данные, необходимые для выполнения операции после восстановления сети.
 * @property createdAt Время создания записи.
 * @property attempts Сколько раз уже пытались синхронизировать эту запись.
 * @property lastError Последняя записанная ошибка синхронизации.
 */
@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entityType: String,
    val entityId: String,
    val operation: String,
    val payload: String,
    val createdAt: Long = System.currentTimeMillis(),
    val attempts: Int = 0,
    val lastError: String? = null,
)

/**
 * Запись очереди фотографии улики, ожидающей загрузки.
 *
 * @property id Идентификатор записи очереди фотографий.
 * @property clueId Идентификатор улики.
 * @property localPath Путь к файлу, сохранённому на устройстве.
 * @property createdAt Время создания записи.
 * @property attempts Сколько раз уже пытались синхронизировать эту запись.
 */
@Entity(tableName = "pending_photo_uploads")
data class PendingPhotoUploadEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clueId: Int,
    val localPath: String,
    val createdAt: Long = System.currentTimeMillis(),
    val attempts: Int = 0,
)
