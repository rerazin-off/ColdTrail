package com.example.mrdetective.domain.usecases.accusation

import com.example.mrdetective.domain.models.Accusation
import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.repositories.AccusationRepository
import javax.inject.Inject

/** Validates case and suspect identifiers and submits the supplied evidence for a verdict. */
class SubmitAccusationUseCase
    @Inject
    constructor(
        private val repository: AccusationRepository,
    ) {
        /** Rejects invalid case or suspect identifiers before submitting the evidence to the repository. */
        suspend operator fun invoke(
            caseId: Int,
            suspectId: Int,
            clueIds: List<Int>,
        ): Result<Accusation> {
            if (caseId <= 0 || suspectId <= 0) {
                return Result.failure(
                    DomainError.Validation("Некорректные данные обвинения"),
                )
            }

            return repository.submitAccusation(
                caseId,
                suspectId,
                clueIds,
            )
        }
    }
