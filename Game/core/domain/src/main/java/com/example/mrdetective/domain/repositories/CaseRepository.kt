package com.example.mrdetective.domain.repositories

import com.example.mrdetective.domain.models.Case

/** Provides case descriptions and starts investigations. */
interface CaseRepository {
    /** Returns the catalog of cases available to the player. */
    suspend fun getAvailableCases(): Result<List<Case>>

    /** Looks up the details of a case by its identifier. */
    suspend fun getCase(id: Int): Result<Case>

    /** Starts or resumes an investigation for the specified case and player. */
    suspend fun startCase(id: Int): Result<Unit>
}
