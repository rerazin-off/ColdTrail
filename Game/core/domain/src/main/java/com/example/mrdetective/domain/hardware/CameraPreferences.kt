package com.example.mrdetective.domain.hardware

import kotlinx.coroutines.flow.Flow

/** Хранит выбор игрока об использовании камеры в головоломках с уликами. */
interface CameraPreferences {
    val cameraEnabled: Flow<Boolean?>

    /** Сохраняет выбор игрока об использовании камеры в головоломках с уликами. */
    suspend fun setCameraEnabled(enabled: Boolean)
}

/** Определяет режим поиска: живая камера или запасная сцена. */
enum class ClueSearchMode { Camera, AskPermission, Picture }

/** Выбирает поиск на сцене, если камера недоступна или выключена игроком. */
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
