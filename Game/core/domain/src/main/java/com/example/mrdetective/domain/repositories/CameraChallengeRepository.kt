package com.example.mrdetective.domain.repositories

import com.example.mrdetective.domain.models.CameraChallenge
import kotlinx.coroutines.flow.Flow

enum class CameraSubmission { Pending, Awarded, Failed }

interface CameraChallengeRepository {
    suspend fun load(caseId: Int): Result<List<CameraChallenge>>

    suspend fun submit(
        challenge: CameraChallenge,
        photoPath: String,
    ): Result<Unit>

    fun observeSubmission(challenge: CameraChallenge): Flow<CameraSubmission>

    fun observeSubmissionError(challenge: CameraChallenge): Flow<String?>
}
