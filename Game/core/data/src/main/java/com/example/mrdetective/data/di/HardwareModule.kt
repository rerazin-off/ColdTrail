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

/** Binds Android camera, vibration, and stored device preferences to domain interfaces. */
@Module
@InstallIn(SingletonComponent::class)
abstract class HardwareModule {
    /** Binds the Android vibrator implementation to the domain signal player. */
    @Binds abstract fun bindMorseSignalPlayer(player: AndroidMorseSignalPlayer): MorseSignalPlayer

    /** Binds DataStore as the persistence source for the vibration preference. */
    @Binds abstract fun bindVibrationPreferences(settings: SettingsData): VibrationPreferences

    /** Binds DataStore as the persistence source for the camera preference. */
    @Binds abstract fun bindCameraPreferences(settings: SettingsData): CameraPreferences

    /** Binds DataStore as the persistence source for the permissions introduction state. */
    @Binds abstract fun bindPuzzlePermissionsPreferences(settings: SettingsData): PuzzlePermissionsPreferences

    /** Supplies the Android camera implementation through Hilt. */
    companion object {
        /** Provides the CameraX implementation for Android preview hosts. */
        @Provides
        fun provideClueCamera(camera: AndroidClueCamera): ClueCamera<Pair<LifecycleOwner, PreviewView>> = camera
    }
}
