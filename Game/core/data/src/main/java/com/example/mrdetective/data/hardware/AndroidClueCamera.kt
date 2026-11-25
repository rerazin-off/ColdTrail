package com.example.mrdetective.data.hardware

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.example.mrdetective.domain.hardware.CameraDirection
import com.example.mrdetective.domain.hardware.CameraDirectionFilter
import com.example.mrdetective.domain.hardware.ClueCamera
import com.example.mrdetective.domain.hardware.ClueCameraState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import kotlin.coroutines.resumeWithException
import kotlin.math.asin
import kotlin.math.atan2

/** Реализует предпросмотр улики, съёмку и отслеживание ориентации на Android. */
class AndroidClueCamera
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
    ) : ClueCamera<Pair<LifecycleOwner, PreviewView>> {
        private val main = ContextCompat.getMainExecutor(context)
        private var controller: LifecycleCameraController? = null

        /** Запускает предпросмотр и обновление ориентации, возвращает объект для освобождения ресурсов. */
        override fun attach(
            host: Pair<LifecycleOwner, PreviewView>,
            onState: (ClueCameraState) -> Unit,
        ): AutoCloseable {
            val (owner, preview) = host
            val sensors = context.getSystemService(SensorManager::class.java)
            val rotationSensor = sensors?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            if (sensors == null || rotationSensor == null) {
                onState(ClueCameraState(error = "На устройстве недоступен датчик направления."))
                return AutoCloseable {}
            }

            val camera =
                LifecycleCameraController(context).apply {
                    cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    setEnabledUseCases(CameraController.IMAGE_CAPTURE)
                }
            var active = true
            var state = ClueCameraState()
            val directionFilter = CameraDirectionFilter()

            fun publish(next: ClueCameraState) {
                state = next
                onState(next)
            }

            val sensorListener =
                object : SensorEventListener {
                    private val matrix = FloatArray(9)

                    override fun onSensorChanged(event: SensorEvent) {
                        SensorManager.getRotationMatrixFromVector(matrix, event.values)
                        // Задняя камера направлена вдоль отрицательной оси Z устройства.
                        val east = -matrix[2]
                        val north = -matrix[5]
                        val up = -matrix[8]
                        val heading = Math.toDegrees(atan2(east.toDouble(), north.toDouble())).toFloat()
                        val elevation = Math.toDegrees(asin(up.coerceIn(-1f, 1f).toDouble())).toFloat()
                        val direction = directionFilter.update(CameraDirection((heading + 360f) % 360f, elevation), event.timestamp)
                        publish(state.copy(direction = direction))
                    }

                    override fun onAccuracyChanged(
                        sensor: Sensor,
                        accuracy: Int,
                    ) = Unit
                }
            val lifecycle =
                object : DefaultLifecycleObserver {
                    override fun onResume(owner: LifecycleOwner) {
                        publish(state.copy(previewReady = preview.previewStreamState.value == PreviewView.StreamState.STREAMING))
                        if (!sensors.registerListener(sensorListener, rotationSensor, SensorManager.SENSOR_DELAY_UI)) {
                            publish(state.copy(error = "Не удалось запустить датчик направления."))
                        }
                    }

                    override fun onPause(owner: LifecycleOwner) {
                        sensors.unregisterListener(sensorListener)
                        directionFilter.reset()
                        publish(state.copy(previewReady = false, direction = null))
                    }
                }
            val stream =
                Observer<PreviewView.StreamState> {
                    publish(state.copy(previewReady = it == PreviewView.StreamState.STREAMING))
                }

            preview.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            onState(state)
            val initialized = camera.initializationFuture
            initialized.addListener({
                if (active) {
                    try {
                        initialized.get()
                        check(camera.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA))
                        camera.bindToLifecycle(owner)
                        preview.controller = camera
                        controller = camera
                        preview.previewStreamState.observe(owner, stream)
                        owner.lifecycle.addObserver(lifecycle)
                    } catch (_: Exception) {
                        publish(ClueCameraState(error = "Не удалось открыть заднюю камеру."))
                    }
                }
            }, main)

            return AutoCloseable {
                active = false
                owner.lifecycle.removeObserver(lifecycle)
                sensors.unregisterListener(sensorListener)
                preview.previewStreamState.removeObserver(stream)
                preview.controller = null
                camera.unbind()
                if (controller === camera) controller = null
            }
        }

        /** Делает снимок подключённой камерой и возвращает путь к локальному файлу. */
        override suspend fun takePhoto(): String {
            val camera = checkNotNull(controller) { "Камера ещё не готова." }
            val image =
                suspendCancellableCoroutine<ImageProxy> { continuation ->
                    camera.takePicture(
                        main,
                        object : ImageCapture.OnImageCapturedCallback() {
                            override fun onCaptureSuccess(image: ImageProxy) {
                                continuation.resume(image) { _, captured, _ -> captured.close() }
                            }

                            override fun onError(exception: ImageCaptureException) {
                                continuation.resumeWithException(exception)
                            }
                        },
                    )
                }
            var file: File? = null
            try {
                return image.use { captured ->
                    withContext(Dispatchers.IO) {
                        val folder = File(context.noBackupFilesDir, "camera-clues").apply { mkdirs() }
                        val target = File.createTempFile("capture-", ".jpg", folder).also { file = it }
                        writePhoto(captured, target)
                        target.absolutePath
                    }
                }
            } catch (error: Throwable) {
                file?.delete()
                throw error
            }
        }

        private fun writePhoto(
            image: ImageProxy,
            target: File,
        ) {
            val original = image.toBitmap()
            try {
                val crop = image.cropRect
                val scale = (1024f / maxOf(crop.width(), crop.height())).coerceAtMost(1f)
                val transform =
                    Matrix().apply {
                        postScale(scale, scale)
                        postRotate(image.imageInfo.rotationDegrees.toFloat())
                    }
                val photo = Bitmap.createBitmap(original, crop.left, crop.top, crop.width(), crop.height(), transform, true)
                try {
                    for (quality in listOf(86, 67, 48)) {
                        check(target.outputStream().use { photo.compress(Bitmap.CompressFormat.JPEG, quality, it) })
                        if (target.length() in 1..1_048_576L) return
                    }
                    error("JPEG exceeds the Storage size limit.")
                } finally {
                    if (photo !== original) photo.recycle()
                }
            } finally {
                original.recycle()
            }
        }
    }
