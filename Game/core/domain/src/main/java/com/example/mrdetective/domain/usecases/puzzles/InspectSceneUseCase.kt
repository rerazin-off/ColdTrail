package com.example.mrdetective.domain.usecases.puzzles

import com.example.mrdetective.domain.repositories.SceneRepository
import com.example.mrdetective.domain.models.DomainError
import javax.inject.Inject

class InspectSceneUseCase @Inject constructor(
    private val repository: SceneRepository
) {
    suspend operator fun invoke(
        caseId: Int,
        sceneId: Int
    ): Result<String> {
        if (caseId <= 0 || sceneId <= 0) {
            return Result.failure(
                DomainError.Validation("Некорректные идентификаторы сцены")
            )
        }

        return repository.getScene(caseId, sceneId)
    }
}