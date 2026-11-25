package com.example.mrdetective.domain.repositories

import com.example.mrdetective.domain.models.MorseChallenges

/** Предоставляет головоломки Морзе для расследования. */
interface MorseChallengeRepository {
    /** Возвращает головоломки Морзе указанного дела и расследования. */
    suspend fun load(caseId: Int): Result<MorseChallenges>
}
