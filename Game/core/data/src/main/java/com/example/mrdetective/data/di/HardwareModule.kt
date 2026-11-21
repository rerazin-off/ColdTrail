package com.example.mrdetective.data.di

import com.example.mrdetective.data.hardware.AndroidMorseSignalPlayer
import com.example.mrdetective.data.local.datastore.SettingsData
import com.example.mrdetective.domain.hardware.MorseSignalPlayer
import com.example.mrdetective.domain.hardware.VibrationPreferences
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class HardwareModule {
    @Binds abstract fun bindMorseSignalPlayer(player: AndroidMorseSignalPlayer): MorseSignalPlayer

    @Binds abstract fun bindVibrationPreferences(settings: SettingsData): VibrationPreferences
}
