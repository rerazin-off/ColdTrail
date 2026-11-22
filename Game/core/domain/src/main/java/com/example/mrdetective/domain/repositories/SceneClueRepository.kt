package com.example.mrdetective.domain.repositories

import com.example.mrdetective.domain.models.CameraChallenge

data class SceneClueImage(val clueId: Int?, val imageUrl: String?)

interface SceneClueRepository {
    suspend fun loadDecoys(targetClueId: Int): List<SceneClueImage>

    suspend fun submit(
        challenge: CameraChallenge,
        selectedClueId: Int,
    ): Result<Unit>
}
