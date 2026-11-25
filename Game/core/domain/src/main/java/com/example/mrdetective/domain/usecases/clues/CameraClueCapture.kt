package com.example.mrdetective.domain.usecases.clues

import com.example.mrdetective.domain.hardware.CameraDirection
import com.example.mrdetective.domain.hardware.ClueCamera
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * Относительная позиция улики в кадре камеры.
 *
 * @property x Нормализованная горизонтальная координата в кадре от 0 до 1.
 * @property y Нормализованная вертикальная координата в кадре от 0 до 1.
 */
data class ClueFramePosition(val x: Float, val y: Float)

/** Выбирает азимут улики за пределами начального кадра камеры. */
fun chooseClueAzimuth(start: CameraDirection): Float? {
    if (start.azimuth !in 0f..360f || start.elevation !in -30f..30f) return null
    // Размещаем улику в стороне света, наиболее удалённой от начального направления.
    val opposite = (start.azimuth + 180f) % 360f
    return (opposite / 90f).roundToInt() * 90f % 360f
}

/** Проецирует направление на улику в координаты кадра, если улика видна. */
fun cluePositionInFrame(
    targetAzimuth: Float,
    direction: CameraDirection,
    alreadyVisible: Boolean = true,
): ClueFramePosition? {
    val delta = ((targetAzimuth - direction.azimuth + 540f) % 360f) - 180f
    val horizontalLimit = if (alreadyVisible) 24f else 20f
    val verticalLimit = if (alreadyVisible) 18f else 15f
    if (delta !in -horizontalLimit..horizontalLimit || direction.elevation !in -verticalLimit..verticalLimit) return null
    // При съёмке весь предмет должен помещаться в видоискателе.
    return ClueFramePosition(x = 0.5f + delta / 80f, y = 0.5f + direction.elevation / 60f)
}

/** Запускает съёмку, только когда искомая улика видна в кадре. */
class CaptureCameraClueUseCase
    @Inject
    constructor() {
        /**
         * Возвращает путь к снимку, только если цель находится в видоискателе.
         *
         * @param targetAzimuth Направление на улику в градусах.
         * @param direction Текущий азимут и наклон камеры.
         * @param camera Камера, которая делает снимок при попадании улики в кадр.
         * @return Путь к снимку либо `null`, если улика не видна.
         */
        suspend operator fun invoke(
            targetAzimuth: Float,
            direction: CameraDirection,
            camera: ClueCamera<*>,
        ): String? {
            if (cluePositionInFrame(targetAzimuth, direction) == null) return null
            return camera.takePhoto()
        }
    }
