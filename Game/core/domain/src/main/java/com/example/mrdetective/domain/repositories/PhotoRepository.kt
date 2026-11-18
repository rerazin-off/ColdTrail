package com.example.mrdetective.domain.repositories

interface PhotoRepository {
    suspend fun savePhoto(
        clueId: Int,
        filePath: String,
    ): Result<Unit>

    suspend fun uploadPending(): Result<Unit>
}
