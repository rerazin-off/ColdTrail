package com.example.mrdetective.domain.repositories

import com.example.mrdetective.domain.models.CodeChallenges

/** Provides numeric puzzles for an investigation. */
interface CodeChallengeRepository {
    /** Returns numeric puzzles for the given case and its investigation. */
    suspend fun load(caseId: Int): Result<CodeChallenges>
}
