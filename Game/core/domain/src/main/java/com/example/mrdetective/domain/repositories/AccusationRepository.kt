package com.example.mrdetective.domain.repositories

import com.example.mrdetective.domain.models.Accusation

/** Позволяет отправлять обвинения и получать вердикты расследований. */
interface AccusationRepository {
    /** Ищет записанный вердикт указанного расследования. */
    suspend fun getResult(caseId: Int): Result<Accusation>

    /** Отправляет выбранного подозреваемого и подтверждающие улики для вынесения вердикта. */
    suspend fun submitAccusation(
        caseId: Int,
        suspectId: Int,
        clueIds: List<Int>,
    ): Result<Accusation>
}
