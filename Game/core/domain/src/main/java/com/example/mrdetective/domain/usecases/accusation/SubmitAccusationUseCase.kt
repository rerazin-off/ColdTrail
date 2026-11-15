package com.example.mrdetective.domain.usecases.accusation

import com.example.mrdetective.domain.models.Accusation
import com.example.mrdetective.domain.repositories.AccusationRepository
import com.example.mrdetective.domain.models.DomainError
import javax.inject.Inject

class SubmitAccusationUseCase @Inject constructor(
    private val repository: AccusationRepository
) {
    suspend operator fun invoke(
        caseId: Int,
        suspectId: Int,
        clueIds: List<Int>
    ): Result<Accusation> {
        if (caseId <= 0 || suspectId <= 0) {
            return Result.failure(
                DomainError.Validation("Некорректные данные обвинения")
            )
        }

        return repository.submitAccusation(
            caseId,
            suspectId,
            clueIds
        )
    }
}