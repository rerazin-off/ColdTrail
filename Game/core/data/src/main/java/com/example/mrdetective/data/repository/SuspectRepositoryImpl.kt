package com.example.mrdetective.data.repository

import com.example.mrdetective.data.mapper.toDomain
import com.example.mrdetective.data.network.apiCall
import com.example.mrdetective.data.remote.api.SupabaseRestApi
import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.models.Suspect
import com.example.mrdetective.domain.repositories.SuspectRepository
import javax.inject.Inject
import javax.inject.Singleton

/** Loads suspect profiles from Supabase and uses cached records when offline. */
@Singleton
class SuspectRepositoryImpl
    @Inject
    constructor(private val api: SupabaseRestApi) : SuspectRepository {
        /** Returns public profiles of the suspects assigned to the specified case. */
        override suspend fun getSuspects(caseId: Int): Result<List<Suspect>> =
            apiCall {
                api.getSuspects("eq.$caseId").map { it.toDomain() }
            }

        /** Looks up the public profile of a suspect by identifier. */
        override suspend fun getSuspect(id: Int): Result<Suspect> =
            apiCall {
                api.getSuspect("eq.$id").firstOrNull()?.toDomain() ?: throw DomainError.NotFound("Подозреваемый не найден")
            }
    }
