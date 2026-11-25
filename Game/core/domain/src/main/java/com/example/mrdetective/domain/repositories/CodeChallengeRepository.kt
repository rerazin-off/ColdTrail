package com.example.mrdetective.domain.repositories

import com.example.mrdetective.domain.models.CodeChallenges

/** Предоставляет числовые головоломки расследования. */
interface CodeChallengeRepository {
    /** Возвращает числовые головоломки указанного дела и расследования. */
    suspend fun load(caseId: Int): Result<CodeChallenges>
}
