package com.example.mrdetective.domain.repositories

import com.example.mrdetective.domain.models.Accusation

interface AccusationRepository {
    suspend fun getResult(caseId: Int): Result<Accusation>

    suspend fun submitAccusation(
        caseId: Int,
        suspectId: Int,
        clueIds: List<Int>,
    ): Result<Accusation>
}
