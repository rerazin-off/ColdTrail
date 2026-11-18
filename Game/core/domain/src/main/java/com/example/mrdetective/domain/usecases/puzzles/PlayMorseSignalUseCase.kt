package com.example.mrdetective.domain.usecases.puzzles

import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.models.domainCatching
import javax.inject.Inject

interface MorseSignalPlayer {
    suspend fun play(pattern: String)
}

class PlayMorseSignalUseCase
    @Inject
    constructor(
        private val player: MorseSignalPlayer,
    ) {
        suspend operator fun invoke(pattern: String): Result<Unit> {
            if (pattern.isBlank()) {
                return Result.failure(
                    DomainError.Validation("Сигнал не задан"),
                )
            }

            return domainCatching {
                player.play(pattern)
            }
        }
    }
