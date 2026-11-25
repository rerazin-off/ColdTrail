package com.example.mrdetective.domain.hardware

import kotlinx.coroutines.flow.Flow

/** Stores the player's preference for using the camera in clue puzzles. */
interface CameraPreferences {
    val cameraEnabled: Flow<Boolean?>

    /** Stores the user's choice of camera mode for clue puzzles. */
    suspend fun setCameraEnabled(enabled: Boolean)
}

/** Selects live camera capture or the alternative scene search. */
enum class ClueSearchMode { Camera, AskPermission, Picture }

/** Chooses scene search when the live camera cannot be used or was turned off. */
fun chooseClueSearchMode(
    cameraEnabled: Boolean?,
    cameraAvailable: Boolean,
    permissionGranted: Boolean,
    permissionDeniedBefore: Boolean,
): ClueSearchMode =
    when {
        !cameraAvailable || cameraEnabled == false -> ClueSearchMode.Picture
        permissionGranted -> ClueSearchMode.Camera
        cameraEnabled != null || permissionDeniedBefore -> ClueSearchMode.Picture
        else -> ClueSearchMode.AskPermission
    }
