package com.example.mrdetective.domain.repositories

/** Идентификатор встроенной сцены поиска улик без камеры. */
const val WAREHOUSE_SCENE_ID = 1

/** Определяет способ загрузки интерактивных сцен дела. */
interface SceneRepository {
    /** Ищет интерактивную сцену по идентификаторам дела и сцены. */
    suspend fun getScene(
        caseId: Int,
        sceneId: Int,
    ): Result<String>
}
