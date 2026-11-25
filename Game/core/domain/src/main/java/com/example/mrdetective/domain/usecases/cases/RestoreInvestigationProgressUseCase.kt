package com.example.mrdetective.domain.usecases.cases

import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.models.InvestigationProgress
import com.example.mrdetective.domain.repositories.ProgressRepository
import javax.inject.Inject

/** Восстанавливает сохранённый прогресс расследования. */
class RestoreInvestigationProgressUseCase
    @Inject
    constructor(
        private val repository: ProgressRepository,
    ) {
        /** Ищет прогресс указанного игрока по делу. */
        suspend operator fun invoke(caseId: Int): Result<InvestigationProgress> {
            if (caseId <= 0) {
                return Result.failure(
                    DomainError.Validation("Некорректный ID дела"),
                )
            }

            return repository.getProgress(caseId)
        }
    }
