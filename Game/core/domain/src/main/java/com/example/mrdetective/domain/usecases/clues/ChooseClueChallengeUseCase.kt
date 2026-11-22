package com.example.mrdetective.domain.usecases.clues

import com.example.mrdetective.domain.repositories.CameraChallengeRepository
import com.example.mrdetective.domain.repositories.MorseChallengeRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

data class ClueChallengeChoice(val puzzleId: Int, val usesCamera: Boolean)

class ChooseClueChallengeUseCase
    @Inject
    constructor(
        private val morse: MorseChallengeRepository,
        private val camera: CameraChallengeRepository,
    ) {
        suspend operator fun invoke(caseId: Int): Result<ClueChallengeChoice?> =
            coroutineScope {
                val morseLoad = async { morse.load(caseId) }
                val cameraLoad = async { camera.load(caseId) }
                val morseResult = morseLoad.await()
                val cameraResult = cameraLoad.await()
                val choices =
                    buildList {
                        morseResult.getOrNull()?.puzzles.orEmpty().forEach {
                            if (!it.solved && !it.rewardClaimed) add(ClueChallengeChoice(it.id, usesCamera = false))
                        }
                        cameraResult.getOrNull().orEmpty().forEach {
                            if (!it.pendingOrCompleted && !it.rewardClaimed) add(ClueChallengeChoice(it.id, usesCamera = true))
                        }
                    }
                val choice = choices.randomOrNull()
                val error = cameraResult.exceptionOrNull() ?: morseResult.exceptionOrNull()
                if (choice == null && error != null) Result.failure(error) else Result.success(choice)
            }
    }
