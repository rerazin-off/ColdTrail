package com.example.mrdetective.domain.repositories

/** Сохраняет фотографии улик и отправляет ожидающие загрузки снимки. */
interface PhotoRepository {
    /** Сохраняет локальную фотографию указанной улики. */
    suspend fun savePhoto(
        clueId: Int,
        filePath: String,
    ): Result<Unit>

    /** Запрашивает отправку фотографий улик, ожидающих синхронизации. */
    suspend fun uploadPending(): Result<Unit>
}
