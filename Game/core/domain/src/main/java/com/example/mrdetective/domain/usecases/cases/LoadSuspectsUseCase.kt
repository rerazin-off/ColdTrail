package com.example.mrdetective.domain.usecases.cases

import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.models.Suspect
import com.example.mrdetective.domain.repositories.SuspectRepository
import javax.inject.Inject

/** Loads the suspects assigned to a case. */
class LoadSuspectsUseCase
    @Inject
    constructor(
        private val repository: SuspectRepository,
    ) {
        /** Returns the public suspect profiles for the case. */
        suspend operator fun invoke(caseId: Int): Result<List<Suspect>> {
            if (caseId <= 0) {
                return Result.failure(
                    DomainError.Validation("Некорректный ID дела"),
                )
            }

            return repository.getSuspects(caseId)
        }
    }
