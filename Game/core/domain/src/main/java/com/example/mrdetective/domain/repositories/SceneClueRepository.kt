package com.example.mrdetective.domain.repositories

import com.example.mrdetective.domain.models.CameraChallenge

/** Identifies a clue image and its label for scene search. */
data class SceneClueImage(val clueId: Int?, val imageUrl: String?, val title: String? = null)

/** Provides alternative scene-search items and submits the selected clue. */
interface SceneClueRepository {
    /** Loads alternative clue images while excluding the target clue. */
    suspend fun loadDecoys(targetClueId: Int): List<SceneClueImage>

    /** Submits the target clue found in the fallback scene. */
    suspend fun submit(
        challenge: CameraChallenge,
        selectedClueId: Int,
    ): Result<Unit>
}
