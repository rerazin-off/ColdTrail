package com.example.mrdetective.domain.repositories

/** Stores clue photos and sends pending uploads. */
interface PhotoRepository {
    /** Persists the local photo associated with a clue. */
    suspend fun savePhoto(
        clueId: Int,
        filePath: String,
    ): Result<Unit>

    /** Requests upload of the clue photos waiting to be synchronized. */
    suspend fun uploadPending(): Result<Unit>
}
