package com.example.mrdetective.data.repository

import com.example.mrdetective.data.mapper.toDomain
import com.example.mrdetective.data.network.apiCall
import com.example.mrdetective.data.remote.api.SupabaseRestApi
import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.models.Suspect
import com.example.mrdetective.domain.repositories.SuspectRepository
import javax.inject.Inject
import javax.inject.Singleton

/** Загружает профили подозреваемых из Supabase и использует кеш без сети. */
@Singleton
class SuspectRepositoryImpl
    @Inject
    constructor(private val api: SupabaseRestApi) : SuspectRepository {
        /** Возвращает публичные профили подозреваемых указанного дела. */
        override suspend fun getSuspects(caseId: Int): Result<List<Suspect>> =
            apiCall {
                api.getSuspects("eq.$caseId").map { it.toDomain() }
            }

        /** Ищет публичный профиль подозреваемого по идентификатору. */
        override suspend fun getSuspect(id: Int): Result<Suspect> =
            apiCall {
                api.getSuspect("eq.$id").firstOrNull()?.toDomain() ?: throw DomainError.NotFound("Подозреваемый не найден")
            }
    }
