package com.example.mrdetective.domain.repositories

import com.example.mrdetective.domain.models.Accusation

/** Provides submission and retrieval of investigation verdicts. */
interface AccusationRepository {
    /** Looks up the recorded verdict for the specified investigation. */
    suspend fun getResult(caseId: Int): Result<Accusation>

    /** Submits a selected suspect and supporting evidence for a verdict. */
    suspend fun submitAccusation(
        caseId: Int,
        suspectId: Int,
        clueIds: List<Int>,
    ): Result<Accusation>
}
