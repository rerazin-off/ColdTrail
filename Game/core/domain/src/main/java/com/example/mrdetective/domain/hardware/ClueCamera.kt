package com.example.mrdetective.domain.hardware

/** Азимут и наклон по данным датчиков ориентации устройства. */
data class CameraDirection(val azimuth: Float, val elevation: Float)

/** Состояние доступности камеры и данные отслеживания ориентации. */
data class ClueCameraState(
    val previewReady: Boolean = false,
    val direction: CameraDirection? = null,
    val error: String? = null,
)

/** Интерфейс предпросмотра, ориентации и съёмки без привязки к Android. */
interface ClueCamera<PreviewHost> {
    /**
     * Подключает предпросмотр и датчики к компоненту вызывающей стороны и возвращает объект для освобождения
     * ресурсов.
     */
    fun attach(
        host: PreviewHost,
        onState: (ClueCameraState) -> Unit,
    ): AutoCloseable

    /** Делает снимок и возвращает путь к локальному файлу. */
    suspend fun takePhoto(): String
}
