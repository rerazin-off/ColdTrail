package com.example.mrdetective.domain.usecases.clues

import com.example.mrdetective.domain.hardware.CameraDirection
import com.example.mrdetective.domain.hardware.ClueCamera
import javax.inject.Inject
import kotlin.math.roundToInt

/** Represents the projected camera position of a clue. */
data class ClueFramePosition(val x: Float, val y: Float)

/** Selects a target heading outside the initial camera view. */
fun chooseClueAzimuth(start: CameraDirection): Float? {
    if (start.azimuth !in 0f..360f || start.elevation !in -30f..30f) return null
    // Place the clue on the cardinal bearing farthest from where the player started.
    val opposite = (start.azimuth + 180f) % 360f
    return (opposite / 90f).roundToInt() * 90f % 360f
}

/** Projects the clue heading onto the camera frame when it is visible. */
fun cluePositionInFrame(
    targetAzimuth: Float,
    direction: CameraDirection,
    alreadyVisible: Boolean = true,
): ClueFramePosition? {
    val delta = ((targetAzimuth - direction.azimuth + 540f) % 360f) - 180f
    val horizontalLimit = if (alreadyVisible) 24f else 20f
    val verticalLimit = if (alreadyVisible) 18f else 15f
    if (delta !in -horizontalLimit..horizontalLimit || direction.elevation !in -verticalLimit..verticalLimit) return null
    // Keep the entire object inside the viewfinder when the shutter is pressed.
    return ClueFramePosition(x = 0.5f + delta / 80f, y = 0.5f + direction.elevation / 60f)
}

/** Triggers the camera shutter only when the target clue is visible in the frame. */
class CaptureCameraClueUseCase
    @Inject
    constructor() {
        /** Returns the local photo path only if the target is inside the viewfinder. */
        suspend operator fun invoke(
            targetAzimuth: Float,
            direction: CameraDirection,
            camera: ClueCamera<*>,
        ): String? {
            if (cluePositionInFrame(targetAzimuth, direction) == null) return null
            return camera.takePhoto()
        }
    }
