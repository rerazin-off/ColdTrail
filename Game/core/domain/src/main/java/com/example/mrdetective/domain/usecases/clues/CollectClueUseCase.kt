package com.example.mrdetective.domain.usecases.clues

import com.example.mrdetective.domain.repositories.ClueRepository
import com.example.mrdetective.domain.models.DomainError
import javax.inject.Inject

class CollectClueUseCase @Inject constructor(
    private val repository: ClueRepository
) {
    suspend operator fun invoke(
        clueId: Int
    ): Result<Unit> {
        if (clueId <= 0) {
            return Result.failure(
                DomainError.Validation("Некорректный ID улики")
            )
        }

        return repository.collectClue(clueId)
    }
}