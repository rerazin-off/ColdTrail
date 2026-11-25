package com.example.mrdetective.domain.usecases.clues

import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.repositories.PhotoRepository
import javax.inject.Inject

/**
 * Сохраняет снимок улики через репозиторий фотографий.
 *
 * @param repository Репозиторий локальных снимков и их загрузки.
 */
class SaveCluePhotoUseCase
    @Inject
    constructor(
        private val repository: PhotoRepository,
    ) {
        /**
         * Передаёт репозиторию фотографий сохранение снимка улики.
         *
         * @param clueId Идентификатор найденной или предъявляемой улики.
         * @param filePath Путь к локальному снимку.
         * @return Подтверждение локального сохранения снимка или ошибка.
         */
        suspend operator fun invoke(
            clueId: Int,
            filePath: String,
        ): Result<Unit> {
            if (clueId <= 0 || filePath.isBlank()) {
                return Result.failure(
                    DomainError.Validation("Некорректные данные фотографии"),
                )
            }

            return repository.savePhoto(clueId, filePath)
        }
    }
