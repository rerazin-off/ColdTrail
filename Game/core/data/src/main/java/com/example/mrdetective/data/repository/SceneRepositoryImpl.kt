package com.example.mrdetective.data.repository

import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.repositories.SceneRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The supplied PostgreSQL schema and REST contract do not define a scenes table or scene endpoint.
 * Keep the interface bound, but fail explicitly instead of inventing server data.
 */
@Singleton
class SceneRepositoryImpl
    @Inject
    constructor() : SceneRepository {
        /** Returns a not-found error because the current API has no scene resource. */
        override suspend fun getScene(
            caseId: Int,
            sceneId: Int,
        ): Result<String> = Result.failure(DomainError.NotFound("В текущей SQL-схеме и API нет таблицы/эндпоинта сцен."))
    }
