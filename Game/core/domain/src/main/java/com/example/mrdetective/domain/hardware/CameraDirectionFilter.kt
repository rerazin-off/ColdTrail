package com.example.mrdetective.domain.hardware

import kotlin.math.abs
import kotlin.math.exp

/** Smooths camera heading and pitch while handling azimuth wraparound. */
class CameraDirectionFilter {
    private var direction: CameraDirection? = null
    private var lastSampleNanos = 0L

    /** Feeds a sensor reading into the filter and returns the smoothed direction. */
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

    /** Clears the previous direction so the next reading starts a fresh sample. */
    fun reset() {
        direction = null
    }

    private fun movement(
        delta: Float,
        weight: Float,
    ): Float = if (abs(delta) < 0.6f) 0f else delta * weight
}
