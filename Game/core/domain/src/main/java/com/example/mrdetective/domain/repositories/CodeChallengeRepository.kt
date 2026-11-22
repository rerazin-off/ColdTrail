package com.example.mrdetective.domain.repositories

import com.example.mrdetective.domain.models.CodeChallenges

interface CodeChallengeRepository {
    suspend fun load(caseId: Int): Result<CodeChallenges>
}
