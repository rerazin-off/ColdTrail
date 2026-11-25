package com.example.mrdetective.domain.usecases.accusation

import com.example.mrdetective.domain.models.Accusation
import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.repositories.AccusationRepository
import javax.inject.Inject

/** Проверяет идентификаторы дела и подозреваемого и отправляет улики для вынесения вердикта. */
class SubmitAccusationUseCase
    @Inject
    constructor(
        private val repository: AccusationRepository,
    ) {
        /** Отклоняет недопустимые идентификаторы дела и подозреваемого до вызова репозитория. */
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
