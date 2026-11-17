package com.example.mrdetective.data.repository

import android.content.Context
import com.example.mrdetective.data.datastore.SettingsDataStore
import com.example.mrdetective.data.local.dao.PendingPhotoDao
import com.example.mrdetective.data.local.entity.PendingPhotoUploadEntity
import com.example.mrdetective.data.network.ConnectivityChecker
import com.example.mrdetective.data.remote.api.OfflineApi
import com.example.mrdetective.data.sync.SyncScheduler
import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.repositories.PhotoRepository
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoRepositoryImpl @Inject constructor(
    private val pending: PendingPhotoDao,
    private val settings: SettingsDataStore,
    private val api: OfflineApi,
    private val connectivity: ConnectivityChecker,
    @ApplicationContext private val context: Context,
) : PhotoRepository {
    override suspend fun savePhoto(clueId: Int, filePath: String): Result<Unit> = try {
        val file = File(filePath)
        if (!file.isFile) throw DomainError.Validation("Файл фотографии не найден")
        if (file.length() > 1_048_576L) throw DomainError.Validation("Фото превышает лимит Storage 1 МБ")
        pending.enqueue(PendingPhotoUploadEntity(clueId = clueId, localPath = file.absolutePath))
        if (connectivity.isNetworkAvailable()) uploadPending() else {
            SyncScheduler.enqueue(context)
            Result.success(Unit)
        }
    } catch (error: Throwable) { Result.failure(error) }

    override suspend fun uploadPending(): Result<Unit> = try {
        val owner = settings.userId.first() ?: throw DomainError.Unauthorized()
        for (photo in pending.pending()) {
            val file = File(photo.localPath)
            if (!file.exists()) { pending.remove(photo.id); continue }
            val extension = file.extension.lowercase().ifBlank { "webp" }
            val mime = when (extension) { "jpg", "jpeg" -> "image/jpeg"; "png" -> "image/png"; else -> "image/webp" }
            api.uploadPhoto(
                "clue-photos", "$owner/${photo.clueId}.$extension",
                file.readBytes().toRequestBody(mime.toMediaType()),
            ).close()
            pending.remove(photo.id)
        }
        Result.success(Unit)
    } catch (error: Throwable) {
        SyncScheduler.enqueue(context)
        Result.failure(error)
    }
}
