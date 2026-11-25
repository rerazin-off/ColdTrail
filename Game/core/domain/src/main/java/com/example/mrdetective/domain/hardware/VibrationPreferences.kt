package com.example.mrdetective.domain.hardware

/** Stores the player's Morse vibration preference. */
interface VibrationPreferences {
    /** A null preference means the player has not chosen a mode yet. */
    suspend fun getVibrationEnabled(): Boolean?

    /** Stores the player's choice of whether to use vibration in Morse puzzles. */
    suspend fun setVibrationEnabled(enabled: Boolean)
}
