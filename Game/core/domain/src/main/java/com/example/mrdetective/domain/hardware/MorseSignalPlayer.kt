package com.example.mrdetective.domain.hardware

interface MorseSignalPlayer {
    fun isAvailable(): Boolean

    suspend fun play(pattern: String)
}
