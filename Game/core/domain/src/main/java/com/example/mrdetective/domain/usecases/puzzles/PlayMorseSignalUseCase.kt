package com.example.mrdetective.domain.usecases.puzzles

import com.example.mrdetective.domain.hardware.MorseSignalPlayer
import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.models.domainCatching
import javax.inject.Inject

private val MorsePattern = Regex("[.-]+(?:\\s+[.-]+|\\s*/\\s*[.-]+)*")

/** Проверяет и воспроизводит сигнал Морзе через интерфейс проигрывателя. */
class PlayMorseSignalUseCase
    @Inject
    constructor(
        private val player: MorseSignalPlayer,
    ) {
        /** Сообщает, доступен ли переданный проигрыватель Морзе. */
        fun isAvailable(): Boolean = player.isAvailable()

        /** Начинает воспроизведение или возвращает ошибку, если сигнал проиграть нельзя. */
        suspend operator fun invoke(pattern: String): Result<Unit> {
            val signal = pattern.trim()
            if (signal.length > 256 || !MorsePattern.matches(signal)) {
                return Result.failure(
                    DomainError.Validation("Некорректный сигнал Морзе"),
                )
            }

            return domainCatching {
                if (!isAvailable()) throw DomainError.HardwareUnavailable()
                player.play(signal)
            }
        }
    }
