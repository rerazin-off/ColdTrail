package com.example.mrdetective.data.repository

import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.repositories.SceneRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * В текущей схеме PostgreSQL и REST API нет ресурса для интерактивных сцен. Реализация подключена к интерфейсу
 * и возвращает явную ошибку.
 */
@Singleton
class SceneRepositoryImpl
    @Inject
    constructor() : SceneRepository {
        /** Возвращает ошибку «Не найдено», поскольку API не предоставляет ресурс сцен. */
        override suspend fun getScene(
            caseId: Int,
            sceneId: Int,
        ): Result<String> = Result.failure(DomainError.NotFound("В текущей SQL-схеме и API нет таблицы/эндпоинта сцен."))
    }
