package com.example.mrdetective.domain.usecases.clues

import com.example.mrdetective.domain.hardware.CameraDirection
import com.example.mrdetective.domain.hardware.ClueCamera
import com.example.mrdetective.domain.hardware.ClueCameraState
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/** Unit tests for the behavior of [CaptureCameraClueUseCase] using controlled inputs and dependencies. */
class CaptureCameraClueUseCaseTest {
    private val camera = FakeClueCamera()
    private val capture = CaptureCameraClueUseCase()

    /** Captures when clue fits the frame. */
    @Test
    fun capturesWhenClueFitsTheFrame() =
        runTest {
            assertEquals("fake-photo.jpg", capture(180f, CameraDirection(180f, 0f), camera))
            assertEquals(1, camera.shots)
        }

    /** Skips the shutter when clue is outside the frame. */
    @Test
    fun skipsTheShutterWhenClueIsOutsideTheFrame() =
        runTest {
            assertNull(capture(180f, CameraDirection(205f, 0f), camera))
            assertNull(capture(180f, CameraDirection(180f, -19f), camera))
            assertEquals(0, camera.shots)
        }

    /** Clue remains visible across north. */
    @Test
    fun clueRemainsVisibleAcrossNorth() {
        assertNotNull(cluePositionInFrame(0f, CameraDirection(359f, 0f)))
        assertNotNull(cluePositionInFrame(0f, CameraDirection(3f, 0f)))
        assertNull(cluePositionInFrame(0f, CameraDirection(180f, 0f)))
    }

    /** Every starting heading requires turning toward acardinal direction. */
    @Test
    fun everyStartingHeadingRequiresTurningTowardACardinalDirection() {
        for (heading in 0..359) {
            val start = CameraDirection(heading.toFloat(), 0f)
            val goal = checkNotNull(chooseClueAzimuth(start))
            assertEquals(0f, goal % 90f, 0f)
            assertNull(cluePositionInFrame(goal, start))
            assertNotNull(cluePositionInFrame(goal, CameraDirection(goal, 0f)))
        }
    }

    /** Visible clue stays in frame near the entry threshold. */
    @Test
    fun visibleClueStaysInFrameNearTheEntryThreshold() {
        val nearEdge = CameraDirection(22f, 0f)
        assertNull(cluePositionInFrame(0f, nearEdge, alreadyVisible = false))
        assertNotNull(cluePositionInFrame(0f, nearEdge, alreadyVisible = true))
        assertNull(cluePositionInFrame(0f, CameraDirection(25f, 0f), alreadyVisible = true))
    }

    /** Waits until camera points toward the room. */
    @Test
    fun waitsUntilCameraPointsTowardTheRoom() {
        assertNull(chooseClueAzimuth(CameraDirection(90f, -60f)))
        assertNull(chooseClueAzimuth(CameraDirection(90f, 45f)))
    }

    /** Test camera that supplies a local photo path and records shutter requests. */
    private class FakeClueCamera : ClueCamera<Unit> {
        var shots = 0
            private set

        /** Supplies a close handle without starting a real Android camera. */
        override fun attach(
            host: Unit,
            onState: (ClueCameraState) -> Unit,
        ): AutoCloseable = AutoCloseable {}

        /** Records the shutter call and returns the fixture photo path. */
        override suspend fun takePhoto(): String {
            shots++
            return "fake-photo.jpg"
        }
    }
}
