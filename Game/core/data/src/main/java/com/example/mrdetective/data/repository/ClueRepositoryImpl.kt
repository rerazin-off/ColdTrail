package com.example.mrdetective.data.repository

import com.example.mrdetective.data.mapper.toDomain
import com.example.mrdetective.data.network.apiCall
import com.example.mrdetective.data.remote.api.SupabaseRestApi
import com.example.mrdetective.domain.models.Clue
import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.repositories.ClueRepository
import javax.inject.Inject
import javax.inject.Singleton

/** Читает метаданные улик и сохраняет найденные улики для синхронизации. */
@Singleton
class ClueRepositoryImpl
    @Inject
    constructor(private val api: SupabaseRestApi) : ClueRepository {
        /** Ищет улику по идентификатору. */
        override suspend fun getClue(id: Int): Result<Clue> =
            apiCall {
                api.getClue("eq.$id").firstOrNull()?.toDomain() ?: throw DomainError.NotFound("Улика не найдена")
            }

        /** Возвращает найденные улики указанного расследования. */
        override suspend fun getCollectedClues(caseId: Int): Result<List<Clue>> =
            Result.failure(
                DomainError.Validation("Нужен investigationId: таблица collected_clues связана с расследованием, а не напрямую с делом."),
            )

        /** Регистрирует улику как найденную в указанном расследовании. */
        override suspend fun collectClue(clueId: Int): Result<Unit> =
            Result.failure(
                DomainError.Validation("Для collectClue требуется investigationId, которого нет в сигнатуре Domain."),
            )
    }
