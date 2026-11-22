package com.example.mrdetective.domain.usecases.clues

import com.example.mrdetective.domain.hardware.CameraDirection
import com.example.mrdetective.domain.hardware.ClueCamera
import javax.inject.Inject

data class ClueFramePosition(val x: Float, val y: Float)

fun cluePositionInFrame(
    puzzleId: Int,
    direction: CameraDirection,
): ClueFramePosition? {
    val targetAzimuth = puzzleId * 137L % 360
    val delta = ((targetAzimuth - direction.azimuth + 540f) % 360f) - 180f
    if (delta !in -24f..24f || direction.elevation !in -18f..18f) return null
    // Keep the entire object inside the viewfinder when the shutter is pressed.
    return ClueFramePosition(x = 0.5f + delta / 80f, y = 0.5f + direction.elevation / 60f)
}

class CaptureCameraClueUseCase
    @Inject
    constructor() {
        suspend operator fun invoke(
            puzzleId: Int,
            direction: CameraDirection,
            camera: ClueCamera<*>,
        ): String? {
            if (cluePositionInFrame(puzzleId, direction) == null) return null
            return camera.takePhoto()
        }
    }
