package com.example.mrdetective.domain.repositories

import com.example.mrdetective.domain.models.InvestigationProgress
import kotlinx.coroutines.flow.Flow

/** Читает, сохраняет, отслеживает и синхронизирует прогресс расследования. */
interface ProgressRepository {
    /** Восстанавливает сохранённый прогресс игрока по указанному делу. */
    suspend fun getProgress(caseId: Int): Result<InvestigationProgress>

    /** Отслеживает изменения списка идентификаторов улик, найденных в расследовании. */
    fun observeCollectedClueIds(caseId: Int): Flow<Set<Int>>

    /** Сохраняет переданный прогресс расследования. */
    suspend fun saveProgress(progress: InvestigationProgress): Result<Unit>

    /** Записывает итоговое состояние расследования. */
    suspend fun completeCase(caseId: Int): Result<Unit>

    /** Запрашивает синхронизацию локально сохранённых изменений. */
    suspend fun syncProgress(): Result<Unit>
}
