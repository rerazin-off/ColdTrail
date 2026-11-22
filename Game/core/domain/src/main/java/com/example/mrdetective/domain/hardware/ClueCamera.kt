package com.example.mrdetective.domain.hardware

data class CameraDirection(val azimuth: Float, val elevation: Float)

data class ClueCameraState(
    val previewReady: Boolean = false,
    val direction: CameraDirection? = null,
    val error: String? = null,
)

interface ClueCamera<PreviewHost> {
    fun attach(
        host: PreviewHost,
        onState: (ClueCameraState) -> Unit,
    ): AutoCloseable

    suspend fun takePhoto(): String
}
