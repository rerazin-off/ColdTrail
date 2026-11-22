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
            assertEquals("fake-photo.jpg", capture(1, CameraDirection(137f, 0f), camera))
            assertEquals(1, camera.shots)
        }

    @Test
    fun skipsTheShutterWhenClueIsOutsideTheFrame() =
        runTest {
            assertNull(capture(1, CameraDirection(162f, 0f), camera))
            assertNull(capture(1, CameraDirection(137f, -19f), camera))
            assertEquals(0, camera.shots)
        }

    @Test
    fun clueRemainsVisibleAcrossNorth() {
        // Puzzle 113 has bearing 1°. Both headings should show the same object.
        assertNotNull(cluePositionInFrame(113, CameraDirection(359f, 0f)))
        assertNotNull(cluePositionInFrame(113, CameraDirection(3f, 0f)))
        assertNull(cluePositionInFrame(113, CameraDirection(180f, 0f)))
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
