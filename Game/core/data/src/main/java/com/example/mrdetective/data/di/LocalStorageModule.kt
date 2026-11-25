package com.example.mrdetective.data.di

import com.example.mrdetective.data.local.AppDatabase
import com.example.mrdetective.data.local.dao.PendingPhotoDao
import com.example.mrdetective.data.local.dao.PuzzleDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Provides DAOs for locally cached puzzles and pending photo uploads. */
@Module
@InstallIn(SingletonComponent::class)
object LocalStorageModule {
    /** Provides the DAO for cached puzzle metadata. */
    @Provides fun providePuzzleDao(db: AppDatabase): PuzzleDao = db.puzzleDao()

    /** Provides the DAO for pending clue photo uploads. */
    @Provides fun providePendingPhotoDao(db: AppDatabase): PendingPhotoDao = db.pendingPhotoDao()
}
