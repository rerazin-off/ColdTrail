package com.example.mrdetective.domain.usecases.cases

import com.example.mrdetective.domain.models.InvestigationProgress
import com.example.mrdetective.domain.repositories.ProgressRepository
import com.example.mrdetective.domain.models.DomainError
import javax.inject.Inject

class RestoreInvestigationProgressUseCase @Inject constructor(
    private val repository: ProgressRepository
) {
    suspend operator fun invoke(
        caseId: Int
    ): Result<InvestigationProgress> {
        if (caseId <= 0) {
            return Result.failure(
                DomainError.Validation("Некорректный ID дела")
            )
        }

        return repository.getProgress(caseId)
    }
}