package com.example.mrdetective.domain.hardware

/** Хранит настройку вибрации для головоломок Морзе. */
interface VibrationPreferences {
    /** Значение `null` означает, что игрок ещё не выбрал режим. */
    suspend fun getVibrationEnabled(): Boolean?

    /** Сохраняет выбор игрока об использовании вибрации в головоломках Морзе. */
    suspend fun setVibrationEnabled(enabled: Boolean)
}
