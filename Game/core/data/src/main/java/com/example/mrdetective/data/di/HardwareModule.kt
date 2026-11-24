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

@Module
@InstallIn(SingletonComponent::class)
abstract class HardwareModule {
    @Binds abstract fun bindMorseSignalPlayer(player: AndroidMorseSignalPlayer): MorseSignalPlayer

    @Binds abstract fun bindVibrationPreferences(settings: SettingsData): VibrationPreferences

    @Binds abstract fun bindCameraPreferences(settings: SettingsData): CameraPreferences

    @Binds abstract fun bindPuzzlePermissionsPreferences(settings: SettingsData): PuzzlePermissionsPreferences

    companion object {
        @Provides
        fun provideClueCamera(camera: AndroidClueCamera): ClueCamera<Pair<LifecycleOwner, PreviewView>> = camera
    }
}
