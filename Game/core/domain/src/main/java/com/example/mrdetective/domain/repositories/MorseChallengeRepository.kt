package com.example.mrdetective.domain.repositories

import com.example.mrdetective.domain.models.MorseChallenges

interface MorseChallengeRepository {
    suspend fun load(caseId: Int): Result<MorseChallenges>
}
