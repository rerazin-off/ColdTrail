package com.example.mrdetective.domain.repositories

import com.example.mrdetective.domain.models.Suspect

/** Ищет публичные профили подозреваемых и список персонажей дела. */
interface SuspectRepository {
    /** Возвращает публичные профили подозреваемых указанного дела. */
    suspend fun getSuspects(caseId: Int): Result<List<Suspect>>

    /** Ищет публичный профиль подозреваемого по идентификатору. */
    suspend fun getSuspect(id: Int): Result<Suspect>
}
