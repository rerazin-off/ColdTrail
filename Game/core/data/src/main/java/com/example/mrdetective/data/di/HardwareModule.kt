package com.example.mrdetective.data.di

import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import com.example.mrdetective.data.hardware.AndroidClueCamera
import com.example.mrdetective.data.hardware.AndroidMorseSignalPlayer
import com.example.mrdetective.data.local.datastore.SettingsData
import com.example.mrdetective.domain.hardware.CameraPreferences
import com.example.mrdetective.domain.hardware.ClueCamera
import com.example.mrdetective.domain.hardware.MorseSignalPlayer
import com.example.mrdetective.domain.hardware.PuzzlePermissionsPreferences
import com.example.mrdetective.domain.hardware.VibrationPreferences
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Связывает Android-реализации камеры, вибрации и настроек устройства с интерфейсами домена. */
@Module
@InstallIn(SingletonComponent::class)
abstract class HardwareModule {
    /** Связывает Android-реализацию вибрации с интерфейсом проигрывателя сигналов. */
    @Binds abstract fun bindMorseSignalPlayer(player: AndroidMorseSignalPlayer): MorseSignalPlayer

    /** Подключает DataStore как хранилище настройки вибрации. */
    @Binds abstract fun bindVibrationPreferences(settings: SettingsData): VibrationPreferences

    /** Подключает DataStore как хранилище настройки камеры. */
    @Binds abstract fun bindCameraPreferences(settings: SettingsData): CameraPreferences

    /** Подключает DataStore для хранения отметки о показе экрана разрешений. */
    @Binds abstract fun bindPuzzlePermissionsPreferences(settings: SettingsData): PuzzlePermissionsPreferences

    /** Предоставляет реализацию камеры Android через Hilt. */
    companion object {
        /** Предоставляет реализацию CameraX для Android-компонентов предпросмотра. */
        @Provides
        fun provideClueCamera(camera: AndroidClueCamera): ClueCamera<Pair<LifecycleOwner, PreviewView>> = camera
    }
}
