package com.example.mrdetective.domain.usecases.cases

import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.repositories.CaseRepository
import javax.inject.Inject

/** Начинает расследование для указанного игрока и дела. */
class StartInvestigationUseCase
    @Inject
    constructor(
        private val repository: CaseRepository,
    ) {
        /** Начинает выбранное дело через репозиторий. */
        suspend operator fun invoke(caseId: Int): Result<Unit> {
            if (caseId <= 0) {
                return Result.failure(
                    DomainError.Validation("Некорректный ID дела"),
                )
            }

            return repository.startCase(caseId)
        }
    }
