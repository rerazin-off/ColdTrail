package com.example.mrdetective.domain.usecases.clues

import com.example.mrdetective.domain.hardware.CameraDirection
import com.example.mrdetective.domain.hardware.ClueCamera
import javax.inject.Inject
import kotlin.math.roundToInt

data class ClueFramePosition(val x: Float, val y: Float)

fun chooseClueAzimuth(start: CameraDirection): Float? {
    if (start.azimuth !in 0f..360f || start.elevation !in -30f..30f) return null
    // Place the clue on the cardinal bearing farthest from where the player started.
    val opposite = (start.azimuth + 180f) % 360f
    return (opposite / 90f).roundToInt() * 90f % 360f
}

fun cluePositionInFrame(
    targetAzimuth: Float,
    direction: CameraDirection,
): ClueFramePosition? {
    val delta = ((targetAzimuth - direction.azimuth + 540f) % 360f) - 180f
    if (delta !in -24f..24f || direction.elevation !in -18f..18f) return null
    // Keep the entire object inside the viewfinder when the shutter is pressed.
    return ClueFramePosition(x = 0.5f + delta / 80f, y = 0.5f + direction.elevation / 60f)
}

class CaptureCameraClueUseCase
    @Inject
    constructor() {
        suspend operator fun invoke(
            targetAzimuth: Float,
            direction: CameraDirection,
            camera: ClueCamera<*>,
        ): String? {
            if (cluePositionInFrame(targetAzimuth, direction) == null) return null
            return camera.takePhoto()
        }
    }
