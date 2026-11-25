package com.example.mrdetective.domain.usecases.cases

import com.example.mrdetective.domain.repositories.PhotoRepository
import com.example.mrdetective.domain.repositories.ProgressRepository
import javax.inject.Inject

/**
 * Запрашивает синхронизацию прогресса расследования.
 *
 * @param progressRepository Репозиторий состояния расследования и найденных улик.
 * @param photoRepository Хранилище снимков улик и их очереди загрузки.
 */
class SyncProgressUseCase
    @Inject
    constructor(
        private val progressRepository: ProgressRepository,
        private val photoRepository: PhotoRepository,
    ) {
        /**
         * Запрашивает синхронизацию прогресса, затем отправку ожидающих загрузки фотографий улик.
         *
         * @return Результат запуска синхронизации. При сбое прогресса фото в этом вызове не отправляются.
         */
        suspend operator fun invoke(): Result<Unit> {
            val progressResult = progressRepository.syncProgress()

            if (progressResult.isFailure) {
                return progressResult
            }

            return photoRepository.uploadPending()
        }
    }
