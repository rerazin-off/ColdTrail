package com.example.mrdetective.data.hardware

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.mrdetective.domain.hardware.MorseSignalPlayer
import com.example.mrdetective.domain.models.DomainError
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/** Преобразует символы и паузы Морзе в последовательность вибраций Android. */
@Singleton
class AndroidMorseSignalPlayer
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) : MorseSignalPlayer {
        @Suppress("DEPRECATION")
        private val vibrator: Vibrator? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(VibratorManager::class.java)?.defaultVibrator
            } else {
                context.getSystemService(Vibrator::class.java)
            }

        private val playback = Mutex()

        /** Сообщает, может ли устройство воспроизводить сигналы Морзе вибрацией. */
        override fun isAvailable(): Boolean = vibrator?.hasVibrator() == true

        /** Воспроизводит заданную последовательность Морзе вибрацией. */
        override suspend fun play(pattern: String) {
            playback.withLock {
                val motor = vibrator?.takeIf { it.hasVibrator() } ?: throw DomainError.HardwareUnavailable()
                val timings = morseWaveform(pattern)
                motor.vibrate(VibrationEffect.createWaveform(timings, -1))
                try {
                    delay(timings.sum())
                } finally {
                    motor.cancel()
                }
            }
        }
    }

private const val DOT_MILLIS = 140L

internal fun morseWaveform(pattern: String): LongArray =
    buildList {
        val words = pattern.trim().split('/')
        for ((wordIndex, word) in words.withIndex()) {
            val letters = word.trim().split(Regex("\\s+"))
            for ((letterIndex, letter) in letters.withIndex()) {
                for ((symbolIndex, symbol) in letter.withIndex()) {
                    val pause =
                        when {
                            symbolIndex > 0 -> 1
                            letterIndex > 0 -> 3
                            wordIndex > 0 -> 7
                            else -> 0
                        }
                    add(pause * DOT_MILLIS)
                    add(if (symbol == '.') DOT_MILLIS else 3 * DOT_MILLIS)
                }
            }
        }
    }.toLongArray()
