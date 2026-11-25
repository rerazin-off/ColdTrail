package com.example.mrdetective.domain.usecases.clues

import com.example.mrdetective.domain.hardware.CameraDirection
import com.example.mrdetective.domain.hardware.ClueCamera
import com.example.mrdetective.domain.hardware.ClueCameraState
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/** Проверяет [CaptureCameraClueUseCase] на заданных входных данных с тестовыми зависимостями. */
class CaptureCameraClueUseCaseTest {
    private val camera = FakeClueCamera()
    private val capture = CaptureCameraClueUseCase()

    /** Проверяет, что камера делает снимок, когда улика помещается в кадре. */
    @Test
    fun capturesWhenClueFitsTheFrame() =
        runTest {
            assertEquals("fake-photo.jpg", capture(180f, CameraDirection(180f, 0f), camera))
            assertEquals(1, camera.shots)
        }

    /** Проверяет, что камера не снимает, если улика находится за пределами кадра. */
    @Test
    fun skipsTheShutterWhenClueIsOutsideTheFrame() =
        runTest {
            assertNull(capture(180f, CameraDirection(205f, 0f), camera))
            assertNull(capture(180f, CameraDirection(180f, -19f), camera))
            assertEquals(0, camera.shots)
        }

    /** Проверяет, что улика остаётся видимой при переходе азимута через север. */
    @Test
    fun clueRemainsVisibleAcrossNorth() {
        assertNotNull(cluePositionInFrame(0f, CameraDirection(359f, 0f)))
        assertNotNull(cluePositionInFrame(0f, CameraDirection(3f, 0f)))
        assertNull(cluePositionInFrame(0f, CameraDirection(180f, 0f)))
    }

    /** Проверяет, что при любом начальном направлении игроку нужно повернуться к одной из сторон света. */
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

    /** Проверяет, что уже видимая улика остаётся в кадре рядом с порогом появления. */
    @Test
    fun visibleClueStaysInFrameNearTheEntryThreshold() {
        val nearEdge = CameraDirection(22f, 0f)
        assertNull(cluePositionInFrame(0f, nearEdge, alreadyVisible = false))
        assertNotNull(cluePositionInFrame(0f, nearEdge, alreadyVisible = true))
        assertNull(cluePositionInFrame(0f, CameraDirection(25f, 0f), alreadyVisible = true))
    }

    /** Проверяет ожидание момента, когда камера будет направлена в комнату. */
    @Test
    fun waitsUntilCameraPointsTowardTheRoom() {
        assertNull(chooseClueAzimuth(CameraDirection(90f, -60f)))
        assertNull(chooseClueAzimuth(CameraDirection(90f, 45f)))
    }

    /** Тестовая камера, возвращающая путь к снимку и запоминающая запросы съёмки. */
    private class FakeClueCamera : ClueCamera<Unit> {
        var shots = 0
            private set

        /** Возвращает объект для закрытия без запуска настоящей камеры Android. */
        override fun attach(
            host: Unit,
            onState: (ClueCameraState) -> Unit,
        ): AutoCloseable = AutoCloseable {}

        /** Запоминает запрос съёмки и возвращает путь к тестовому снимку. */
        override suspend fun takePhoto(): String {
            shots++
            return "fake-photo.jpg"
        }
    }
}
