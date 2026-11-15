package com.example.mrdetective.domain.repositories

import com.example.mrdetective.domain.models.Suspect

interface SuspectRepository {
    suspend fun getSuspects(caseId: Int): Result<List<Suspect>>

    suspend fun getSuspect(id: Int): Result<Suspect>
}
