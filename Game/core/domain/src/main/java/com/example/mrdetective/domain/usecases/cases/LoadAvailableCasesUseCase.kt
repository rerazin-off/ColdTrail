package com.example.mrdetective.domain.usecases.cases

import com.example.mrdetective.domain.models.Case
import com.example.mrdetective.domain.repositories.CaseRepository
import javax.inject.Inject

/** Loads the case catalog through the domain repository. */
class LoadAvailableCasesUseCase
    @Inject
    constructor(
        private val repository: CaseRepository,
    ) {
        /** Returns the cases supplied by the case repository. */
        suspend operator fun invoke(): Result<List<Case>> {
            return repository.getAvailableCases()
        }
    }
