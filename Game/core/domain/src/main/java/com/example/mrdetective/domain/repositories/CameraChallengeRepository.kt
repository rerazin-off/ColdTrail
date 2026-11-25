package com.example.mrdetective.domain.repositories

import com.example.mrdetective.domain.models.CameraChallenge
import kotlinx.coroutines.flow.Flow

/** Показывает, принят ли снимок улики или он ожидает синхронизации. */
enum class CameraSubmission { Pending, Awarded, Failed }

/** Загружает головоломки с камерой и отслеживает отправку снимков. */
interface CameraChallengeRepository {
    /** Загружает доступные головоломки с камерой для указанного дела. */
    suspend fun load(caseId: Int): Result<List<CameraChallenge>>

    /** Отправляет локальный снимок для выбранной головоломки с камерой. */
    suspend fun submit(
        challenge: CameraChallenge,
        photoPath: String,
    ): Result<Unit>

    /** Отслеживает, находится ли отправка снимка в очереди или уже принята. */
    fun observeSubmission(challenge: CameraChallenge): Flow<CameraSubmission>

    /** Отслеживает ошибку отправки снимка, если она есть. */
    fun observeSubmissionError(challenge: CameraChallenge): Flow<String?>
}
