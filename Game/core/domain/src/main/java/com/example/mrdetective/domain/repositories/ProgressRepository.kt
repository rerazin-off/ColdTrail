package com.example.mrdetective.domain.repositories

import com.example.mrdetective.domain.models.InvestigationProgress
import kotlinx.coroutines.flow.Flow

/** Reads, writes, observes, and synchronizes investigation progress. */
interface ProgressRepository {
    /** Restores the saved progress for the given case and player. */
    suspend fun getProgress(caseId: Int): Result<InvestigationProgress>

    /** Observes the identifiers of clues earned in the selected investigation. */
    fun observeCollectedClueIds(caseId: Int): Flow<Set<Int>>

    /** Persists the supplied investigation progress. */
    suspend fun saveProgress(progress: InvestigationProgress): Result<Unit>

    /** Records the final state of the investigation. */
    suspend fun completeCase(caseId: Int): Result<Unit>

    /** Requests synchronization of the locally stored changes. */
    suspend fun syncProgress(): Result<Unit>
}
