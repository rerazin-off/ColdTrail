package com.example.mrdetective.domain.hardware

interface VibrationPreferences {
    /** A null preference means the player has not chosen a mode yet. */
    suspend fun getVibrationEnabled(): Boolean?

    suspend fun setVibrationEnabled(enabled: Boolean)
}
