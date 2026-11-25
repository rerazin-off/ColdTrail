package com.example.mrdetective.data.di

import android.content.Context
import androidx.room3.Room
import com.example.mrdetective.data.local.AppDatabase
import com.example.mrdetective.data.local.dao.CaseDao
import com.example.mrdetective.data.local.dao.ClueDao
import com.example.mrdetective.data.local.dao.DialogueDao
import com.example.mrdetective.data.local.dao.InvestigationDao
import com.example.mrdetective.data.local.dao.NoteDao
import com.example.mrdetective.data.local.dao.SuspectDao
import com.example.mrdetective.data.local.dao.SyncQueueDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Provides the singleton Room database and its investigation DAOs. */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    /** Builds the Room database with the registered schema migrations. */
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "coldtrail.db",
        )
            .addMigrations(
                AppDatabase.MIGRATION_1_2,
                AppDatabase.MIGRATION_2_3,
            )
            .build()
    }

    /** Provides the DAO for cached cases. */
    @Provides
    fun provideCaseDao(database: AppDatabase): CaseDao = database.caseDao()

    /** Provides the DAO for cached suspect profiles. */
    @Provides
    fun provideSuspectDao(database: AppDatabase): SuspectDao = database.suspectDao()

    /** Provides the DAO for cached clues and inventory entries. */
    @Provides
    fun provideClueDao(database: AppDatabase): ClueDao = database.clueDao()

    /** Provides the DAO for local investigations and their progress. */
    @Provides
    fun provideInvestigationDao(database: AppDatabase): InvestigationDao = database.investigationDao()

    /** Provides the DAO for cached conversations. */
    @Provides
    fun provideDialogueDao(database: AppDatabase): DialogueDao = database.dialogueDao()

    /** Provides the DAO for local investigation notes. */
    @Provides
    fun provideNoteDao(database: AppDatabase): NoteDao = database.noteDao()

    /** Provides the DAO for pending synchronization operations. */
    @Provides
    fun provideSyncQueueDao(database: AppDatabase): SyncQueueDao = database.syncQueueDao()
}
