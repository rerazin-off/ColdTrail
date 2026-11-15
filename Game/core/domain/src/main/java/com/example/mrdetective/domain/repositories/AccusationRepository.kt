package com.example.mrdetective.domain.repositories

import com.example.mrdetective.domain.models.Accusation

interface AccusationRepository {
    suspend fun submitAccusation(
        caseId: Int,
        suspectId: Int,
        clueIds: List<Int>
    ): Result<Accusation>
}