package com.example.mrdetective.domain.usecases.cases

import com.example.mrdetective.domain.repositories.PhotoRepository
import com.example.mrdetective.domain.repositories.ProgressRepository
import javax.inject.Inject

/** Requests synchronization of investigation progress. */
class SyncProgressUseCase
    @Inject
    constructor(
        private val progressRepository: ProgressRepository,
        private val photoRepository: PhotoRepository,
    ) {
        /** Requests progress synchronization before asking for the pending clue photos to be uploaded. */
        suspend operator fun invoke(): Result<Unit> {
            val progressResult = progressRepository.syncProgress()

            if (progressResult.isFailure) {
                return progressResult
            }

            return photoRepository.uploadPending()
        }
    }
