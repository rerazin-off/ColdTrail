package com.example.mrdetective.domain.hardware

import kotlin.math.abs
import kotlin.math.exp

/** Сглаживает азимут и наклон камеры с учётом перехода через север. */
class CameraDirectionFilter {
    private var direction: CameraDirection? = null
    private var lastSampleNanos = 0L

    /** Принимает показание датчика и возвращает сглаженное направление. */
    fun update(
        reading: CameraDirection,
        timestampNanos: Long,
    ): CameraDirection {
        val previous = direction
        val elapsed = timestampNanos - lastSampleNanos
        lastSampleNanos = timestampNanos
        if (previous == null || elapsed <= 0 || elapsed > 500_000_000L) {
            direction = reading
            return reading
        }

        val weight = 1f - exp(-elapsed / 120_000_000f)
        val azimuthDelta = (reading.azimuth - previous.azimuth + 540f) % 360f - 180f
        val elevationDelta = reading.elevation - previous.elevation
        val result =
            CameraDirection(
                azimuth = (previous.azimuth + movement(azimuthDelta, weight) + 360f) % 360f,
                elevation = previous.elevation + movement(elevationDelta, weight),
            )
        direction = result
        return result
    }

    /** Сбрасывает предыдущее направление перед следующим измерением. */
    fun reset() {
        direction = null
    }

    private fun movement(
        delta: Float,
        weight: Float,
    ): Float = if (abs(delta) < 0.6f) 0f else delta * weight
}
