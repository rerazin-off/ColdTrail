package com.example.mrdetective.data.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalStorageModule {
    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "coldtrail.db")
            .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3)
            .build()

    @Provides fun provideCaseDao(db: AppDatabase): CaseDao = db.caseDao()
    @Provides fun provideSuspectDao(db: AppDatabase): SuspectDao = db.suspectDao()
    @Provides fun provideClueDao(db: AppDatabase): ClueDao = db.clueDao()
    @Provides fun provideProgressDao(db: AppDatabase): ProgressDao = db.progressDao()
    @Provides fun provideDialogueDao(db: AppDatabase): DialogueDao = db.dialogueDao()
    @Provides fun provideNoteDao(db: AppDatabase): NoteDao = db.noteDao()
    @Provides fun providePuzzleDao(db: AppDatabase): PuzzleDao = db.puzzleDao()
    @Provides fun provideSyncQueueDao(db: AppDatabase): SyncQueueDao = db.syncQueueDao()
    @Provides fun providePendingPhotoDao(db: AppDatabase): PendingPhotoDao = db.pendingPhotoDao()
}