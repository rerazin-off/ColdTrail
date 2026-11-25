package com.example.mrdetective.domain.hardware

/** Интерфейс воспроизведения Морзе вибрацией без привязки к устройству. */
interface MorseSignalPlayer {
    /** Сообщает, может ли устройство воспроизводить сигналы Морзе вибрацией. */
    fun isAvailable(): Boolean

    /** Воспроизводит заданную последовательность Морзе вибрацией. */
    suspend fun play(pattern: String)
}
