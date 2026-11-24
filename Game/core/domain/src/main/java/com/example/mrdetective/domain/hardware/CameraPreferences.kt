package com.example.mrdetective.domain.hardware

import kotlinx.coroutines.flow.Flow

interface CameraPreferences {
    val cameraEnabled: Flow<Boolean?>

    suspend fun setCameraEnabled(enabled: Boolean)
}

enum class ClueSearchMode { Camera, AskPermission, Picture }

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
