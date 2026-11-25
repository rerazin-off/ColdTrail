package com.example.mrdetective.domain.usecases.cases

import com.example.mrdetective.domain.repositories.PhotoRepository
import com.example.mrdetective.domain.repositories.ProgressRepository
import javax.inject.Inject

/** Запрашивает синхронизацию прогресса расследования. */
class SyncProgressUseCase
    @Inject
    constructor(
        private val progressRepository: ProgressRepository,
        private val photoRepository: PhotoRepository,
    ) {
        /** Запрашивает синхронизацию прогресса, затем отправку ожидающих загрузки фотографий улик. */
        suspend operator fun invoke(): Result<Unit> {
            val progressResult = progressRepository.syncProgress()

            if (progressResult.isFailure) {
                return progressResult
            }

            return photoRepository.uploadPending()
        }
    }
