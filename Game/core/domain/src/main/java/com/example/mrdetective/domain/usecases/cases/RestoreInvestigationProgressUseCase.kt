package com.example.mrdetective.domain.usecases.cases

import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.models.InvestigationProgress
import com.example.mrdetective.domain.repositories.ProgressRepository
import javax.inject.Inject

/** Restores the saved progress of an investigation. */
class RestoreInvestigationProgressUseCase
    @Inject
    constructor(
        private val repository: ProgressRepository,
    ) {
        /** Looks up the specified player's progress on a case. */
        suspend operator fun invoke(caseId: Int): Result<InvestigationProgress> {
            if (caseId <= 0) {
                return Result.failure(
                    DomainError.Validation("Некорректный ID дела"),
                )
            }

            return repository.getProgress(caseId)
        }
    }
