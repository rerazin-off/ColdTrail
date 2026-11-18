package com.example.mrdetective.data.repository

import com.example.mrdetective.data.mapper.toDomain
import com.example.mrdetective.data.network.apiCall
import com.example.mrdetective.data.remote.api.SupabaseRestApi
import com.example.mrdetective.domain.models.Clue
import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.repositories.ClueRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseClueRepositoryImpl @Inject constructor(private val api: SupabaseRestApi) : ClueRepository {
    override suspend fun getClue(id: Int): Result<Clue> = apiCall {
        api.getClue("eq.$id").firstOrNull()?.toDomain() ?: throw DomainError.NotFound("Улика не найдена")
    }
    override suspend fun getCollectedClues(caseId: Int): Result<List<Clue>> = Result.failure(
        DomainError.Validation("Нужен investigationId: таблица collected_clues связана с расследованием, а не напрямую с делом.")
    )
    override suspend fun collectClue(clueId: Int): Result<Unit> = Result.failure(
        DomainError.Validation("Для collectClue требуется investigationId, которого нет в сигнатуре Domain.")
    )
}
