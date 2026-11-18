package com.example.mrdetective.domain.repositories

import com.example.mrdetective.domain.models.Case

interface CaseRepository {
    suspend fun getAvailableCases(): Result<List<Case>>

    suspend fun getCase(id: Int): Result<Case>

    suspend fun startCase(id: Int): Result<Unit>
}
