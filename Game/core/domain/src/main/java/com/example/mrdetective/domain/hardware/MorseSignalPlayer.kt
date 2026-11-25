package com.example.mrdetective.domain.hardware

/** Device-independent interface for playing Morse signals as vibration. */
interface MorseSignalPlayer {
    /** Reports whether this device can play Morse signals using its vibrator. */
    fun isAvailable(): Boolean

    /** Plays the supplied Morse pattern using vibration. */
    suspend fun play(pattern: String)
}
