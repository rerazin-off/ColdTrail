package com.example.mrdetective.domain.repositories

import com.example.mrdetective.domain.models.Clue

/** Предоставляет описания улик и регистрирует находки по расследованию. */
interface ClueRepository {
    /** Ищет улику по идентификатору. */
    suspend fun getClue(id: Int): Result<Clue>

    /** Возвращает найденные улики указанного расследования. */
    suspend fun getCollectedClues(caseId: Int): Result<List<Clue>>

    /** Регистрирует улику как найденную в указанном расследовании. */
    suspend fun collectClue(clueId: Int): Result<Unit>
}
