package com.example.mrdetective.data.local.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

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

@Entity(tableName = "pending_photo_uploads")
data class PendingPhotoUploadEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clueId: Int,
    val localPath: String,
    val createdAt: Long = System.currentTimeMillis(),
    val attempts: Int = 0,
)