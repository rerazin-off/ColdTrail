package com.example.mrdetective.domain.repositories

import com.example.mrdetective.domain.models.Case

/** Предоставляет описания дел и запускает расследования. */
interface CaseRepository {
    /** Возвращает список доступных игроку дел. */
    suspend fun getAvailableCases(): Result<List<Case>>

    /** Ищет подробности дела по идентификатору. */
    suspend fun getCase(id: Int): Result<Case>

    /** Начинает или продолжает расследование указанного дела и игрока. */
    suspend fun startCase(id: Int): Result<Unit>
}
