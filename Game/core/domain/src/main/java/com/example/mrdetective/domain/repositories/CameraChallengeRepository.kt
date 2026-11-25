package com.example.mrdetective.domain.repositories

import com.example.mrdetective.domain.models.CameraChallenge
import kotlinx.coroutines.flow.Flow

/** Tracks whether a captured clue was accepted or is waiting for synchronization. */
enum class CameraSubmission { Pending, Awarded, Failed }

/** Loads camera puzzles and tracks submission of captured photos. */
interface CameraChallengeRepository {
    /** Loads the camera puzzles available for the given case. */
    suspend fun load(caseId: Int): Result<List<CameraChallenge>>

    /** Submits the local photo for the selected camera puzzle. */
    suspend fun submit(
        challenge: CameraChallenge,
        photoPath: String,
    ): Result<Unit>

    /** Observes the queued or accepted state of the camera puzzle submission. */
    fun observeSubmission(challenge: CameraChallenge): Flow<CameraSubmission>

    /** Observes the error associated with the camera puzzle submission, when present. */
    fun observeSubmissionError(challenge: CameraChallenge): Flow<String?>
}
