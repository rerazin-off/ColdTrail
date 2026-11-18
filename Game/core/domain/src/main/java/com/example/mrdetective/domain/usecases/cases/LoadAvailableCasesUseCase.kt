package com.example.mrdetective.domain.usecases.cases

import com.example.mrdetective.domain.models.Case
import com.example.mrdetective.domain.repositories.CaseRepository
import javax.inject.Inject

class LoadAvailableCasesUseCase
    @Inject
    constructor(
        private val repository: CaseRepository,
    ) {
        suspend operator fun invoke(): Result<List<Case>> {
            return repository.getAvailableCases()
        }
    }
