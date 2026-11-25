package com.example.mrdetective.domain.repositories

/** Defines lookup of interactive scenes for a case. */
interface SceneRepository {
    /** Looks up the interactive scene for the given case and scene identifiers. */
    suspend fun getScene(
        caseId: Int,
        sceneId: Int,
    ): Result<String>
}
