package com.example.mrdetective.domain.repositories

import com.example.mrdetective.domain.models.InvestigationProgress
import kotlinx.coroutines.flow.Flow

interface ProgressRepository {
    suspend fun getProgress(caseId: Int): Result<InvestigationProgress>

    fun observeCollectedClueIds(caseId: Int): Flow<Set<Int>>

    suspend fun saveProgress(progress: InvestigationProgress): Result<Unit>

    suspend fun completeCase(caseId: Int): Result<Unit>

    suspend fun syncProgress(): Result<Unit>
}
