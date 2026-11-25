package com.example.mrdetective.domain.usecases.clues

import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.repositories.PhotoRepository
import javax.inject.Inject

/** Saves a captured clue photo through the photo repository. */
class SaveCluePhotoUseCase
    @Inject
    constructor(
        private val repository: PhotoRepository,
    ) {
        /** Delegates persistence of the captured clue photo to the photo repository. */
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
