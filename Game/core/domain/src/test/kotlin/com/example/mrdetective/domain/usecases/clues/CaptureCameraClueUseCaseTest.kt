package com.example.mrdetective.domain.usecases.clues

import com.example.mrdetective.domain.hardware.CameraDirection
import com.example.mrdetective.domain.hardware.ClueCamera
import com.example.mrdetective.domain.hardware.ClueCameraState
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class CaptureCameraClueUseCaseTest {
    private val camera = FakeClueCamera()
    private val capture = CaptureCameraClueUseCase()

    @Test
    fun capturesWhenClueFitsTheFrame() =
        runTest {
            assertEquals("fake-photo.jpg", capture(180f, CameraDirection(180f, 0f), camera))
            assertEquals(1, camera.shots)
        }

    @Test
    fun skipsTheShutterWhenClueIsOutsideTheFrame() =
        runTest {
            assertNull(capture(180f, CameraDirection(205f, 0f), camera))
            assertNull(capture(180f, CameraDirection(180f, -19f), camera))
            assertEquals(0, camera.shots)
        }

    @Test
    fun clueRemainsVisibleAcrossNorth() {
        assertNotNull(cluePositionInFrame(0f, CameraDirection(359f, 0f)))
        assertNotNull(cluePositionInFrame(0f, CameraDirection(3f, 0f)))
        assertNull(cluePositionInFrame(0f, CameraDirection(180f, 0f)))
    }

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

    @Test
    fun waitsUntilCameraPointsTowardTheRoom() {
        assertNull(chooseClueAzimuth(CameraDirection(90f, -60f)))
        assertNull(chooseClueAzimuth(CameraDirection(90f, 45f)))
    }

    private class FakeClueCamera : ClueCamera<Unit> {
        var shots = 0
            private set

        override fun attach(
            host: Unit,
            onState: (ClueCameraState) -> Unit,
        ): AutoCloseable = AutoCloseable {}

        override suspend fun takePhoto(): String {
            shots++
            return "fake-photo.jpg"
        }
    }
}
