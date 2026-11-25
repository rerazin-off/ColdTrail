package com.example.mrdetective.domain.repositories

import com.example.mrdetective.domain.models.MorseChallenges

/** Provides Morse puzzles for an investigation. */
interface MorseChallengeRepository {
    /** Returns Morse puzzles for the given case and its investigation. */
    suspend fun load(caseId: Int): Result<MorseChallenges>
}
