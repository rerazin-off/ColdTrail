package com.example.mrdetective.domain.repositories

/** Определяет способ загрузки интерактивных сцен дела. */
interface SceneRepository {
    /** Ищет интерактивную сцену по идентификаторам дела и сцены. */
    suspend fun getScene(
        caseId: Int,
        sceneId: Int,
    ): Result<String>
}
