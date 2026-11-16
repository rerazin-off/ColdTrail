package com.example.mrdetective.data.repository

import com.example.mrdetective.data.mapper.toDomain
import com.example.mrdetective.data.network.apiCall
import com.example.mrdetective.data.remote.api.SupabaseRestApi
import com.example.mrdetective.domain.models.Case
import com.example.mrdetective.domain.models.Clue
import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.repositories.CaseRepository
import com.example.mrdetective.domain.repositories.ClueRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseCaseRepositoryImpl @Inject constructor(private val api: SupabaseRestApi) : CaseRepository {
    override suspend fun getAvailableCases(): Result<List<Case>> = apiCall { api.getCases().map { it.toDomain() } }

    override suspend fun getCase(id: Int): Result<Case> = apiCall {
        api.getCase("eq.$id").firstOrNull()?.toDomain() ?: throw DomainError.NotFound("Дело не найдено")
    }

    // The SQL schema has no RPC/Edge Function for starting an investigation. Do not fabricate one.
    override suspend fun startCase(id: Int): Result<Unit> = Result.failure(
        DomainError.Validation("Для startCase требуется серверный контракт создания расследования.")
    )
}
