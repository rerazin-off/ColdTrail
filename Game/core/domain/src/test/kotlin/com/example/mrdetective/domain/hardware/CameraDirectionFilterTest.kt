package com.example.mrdetective.domain.hardware

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Tests smoothing and heading wraparound at north. */
class CameraDirectionFilterTest {
    /** Suppresses small sensor fluctuations. */
    @Test
    fun suppressesSmallSensorFluctuations() {
        val filter = CameraDirectionFilter()
        val initial = CameraDirection(90f, 2f)
        filter.update(initial, 0L)

        assertEquals(initial, filter.update(CameraDirection(90.3f, 1.7f), 60_000_000L))
    }

    /** Follows turns across north without jumping. */
    @Test
    fun followsTurnsAcrossNorthWithoutJumping() {
        val filter = CameraDirectionFilter()
        filter.update(CameraDirection(358f, 0f), 0L)

        val firstStep = filter.update(CameraDirection(2f, 10f), 60_000_000L)
        assertTrue(firstStep.azimuth in 358f..360f)
        assertTrue(firstStep.elevation > 0f && firstStep.elevation < 10f)
        val nextStep = filter.update(CameraDirection(2f, 10f), 120_000_000L)
        assertTrue(nextStep.azimuth in 0f..2f)
    }
}
