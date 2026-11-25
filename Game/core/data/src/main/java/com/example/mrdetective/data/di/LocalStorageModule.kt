package com.example.mrdetective.data.di

import com.example.mrdetective.data.local.AppDatabase
import com.example.mrdetective.data.local.dao.PendingPhotoDao
import com.example.mrdetective.data.local.dao.PuzzleDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Предоставляет DAO для кеша головоломок и очереди загрузки фотографий. */
@Module
@InstallIn(SingletonComponent::class)
object LocalStorageModule {
    /** Предоставляет DAO для кеша метаданных головоломок. */
    @Provides fun providePuzzleDao(db: AppDatabase): PuzzleDao = db.puzzleDao()

    /** Предоставляет DAO для очереди загрузки фотографий улик. */
    @Provides fun providePendingPhotoDao(db: AppDatabase): PendingPhotoDao = db.pendingPhotoDao()
}
