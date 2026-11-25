package com.example.mrdetective.domain.hardware

/** Heading and pitch reported by the device orientation sensors. */
data class CameraDirection(val azimuth: Float, val elevation: Float)

/** Availability and tracking information emitted by the clue camera. */
data class ClueCameraState(
    val previewReady: Boolean = false,
    val direction: CameraDirection? = null,
    val error: String? = null,
)

/** Device-independent port for camera preview, orientation, and photo capture. */
interface ClueCamera<PreviewHost> {
    /** Attaches preview and direction tracking to the caller's host and returns a handle for releasing them. */
    fun attach(
        host: PreviewHost,
        onState: (ClueCameraState) -> Unit,
    ): AutoCloseable

    /** Captures a photo and returns its local path. */
    suspend fun takePhoto(): String
}
