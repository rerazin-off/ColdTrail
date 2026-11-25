package com.example.mrdetective.domain.usecases.clues

import com.example.mrdetective.domain.repositories.CameraChallengeRepository
import com.example.mrdetective.domain.repositories.CodeChallengeRepository
import com.example.mrdetective.domain.repositories.MorseChallengeRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

/** Enumerates the puzzle types available from the clue picker. */
enum class ClueChallengeKind { Morse, Camera, Code }

/** Identifies the puzzle selected for the next clue attempt. */
data class ClueChallengeChoice(val puzzleId: Int, val kind: ClueChallengeKind)

/** Chooses an eligible clue puzzle that has not been solved or claimed. */
class ChooseClueChallengeUseCase
    @Inject
    constructor(
        private val morse: MorseChallengeRepository,
        private val camera: CameraChallengeRepository,
        private val code: CodeChallengeRepository,
    ) {
        /** Returns one available clue challenge from the supported types, if any remain. */
        suspend operator fun invoke(caseId: Int): Result<ClueChallengeChoice?> =
            coroutineScope {
                val morseLoad = async { morse.load(caseId) }
                val cameraLoad = async { camera.load(caseId) }
                val codeLoad = async { code.load(caseId) }
                val morseResult = morseLoad.await()
                val cameraResult = cameraLoad.await()
                val codeResult = codeLoad.await()
                val choices =
                    buildList {
                        morseResult.getOrNull()?.puzzles.orEmpty().forEach {
                            if (!it.solved && !it.rewardClaimed) add(ClueChallengeChoice(it.id, ClueChallengeKind.Morse))
                        }
                        cameraResult.getOrNull().orEmpty().forEach {
                            if (!it.pendingOrCompleted && !it.rewardClaimed) add(ClueChallengeChoice(it.id, ClueChallengeKind.Camera))
                        }
                        codeResult.getOrNull()?.puzzles.orEmpty().forEach {
                            if (!it.solved && !it.rewardClaimed) add(ClueChallengeChoice(it.id, ClueChallengeKind.Code))
                        }
                    }
                val choice = choices.randomOrNull()
                val error = cameraResult.exceptionOrNull() ?: morseResult.exceptionOrNull() ?: codeResult.exceptionOrNull()
                if (choice == null && error != null) Result.failure(error) else Result.success(choice)
            }
    }
