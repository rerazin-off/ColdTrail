package com.example.mrdetective.domain.usecases.puzzles

import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.repositories.SceneRepository
import javax.inject.Inject

/** Загружает интерактивную сцену через репозиторий сцен. */
class InspectSceneUseCase
    @Inject
    constructor(
        private val repository: SceneRepository,
    ) {
        /** Запрашивает выбранную сцену у репозитория. */
        suspend operator fun invoke(
            caseId: Int,
            sceneId: Int,
        ): Result<String> {
            if (caseId <= 0 || sceneId <= 0) {
                return Result.failure(
                    DomainError.Validation("Некорректные идентификаторы сцены"),
                )
            }

            return repository.getScene(caseId, sceneId)
        }
    }
