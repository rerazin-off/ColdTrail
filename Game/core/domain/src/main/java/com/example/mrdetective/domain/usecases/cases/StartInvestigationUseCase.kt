package com.example.mrdetective.domain.usecases.cases

import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.repositories.CaseRepository
import javax.inject.Inject

/** Starts an investigation for the given player and case. */
class StartInvestigationUseCase
    @Inject
    constructor(
        private val repository: CaseRepository,
    ) {
        /** Starts the selected case through the case repository. */
        suspend operator fun invoke(caseId: Int): Result<Unit> {
            if (caseId <= 0) {
                return Result.failure(
                    DomainError.Validation("Некорректный ID дела"),
                )
            }

            return repository.startCase(caseId)
        }
    }
