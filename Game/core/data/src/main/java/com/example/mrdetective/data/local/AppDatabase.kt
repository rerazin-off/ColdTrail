package com.example.mrdetective.data.local

import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.example.mrdetective.data.local.dao.CaseDao
import com.example.mrdetective.data.local.dao.ClueDao
import com.example.mrdetective.data.local.dao.DialogueDao
import com.example.mrdetective.data.local.dao.InvestigationDao
import com.example.mrdetective.data.local.dao.NoteDao
import com.example.mrdetective.data.local.dao.PendingPhotoDao
import com.example.mrdetective.data.local.dao.PuzzleDao
import com.example.mrdetective.data.local.dao.SuspectDao
import com.example.mrdetective.data.local.dao.SyncQueueDao
import com.example.mrdetective.data.local.entity.CaseEntity
import com.example.mrdetective.data.local.entity.ClueEntity
import com.example.mrdetective.data.local.entity.CollectedClueEntity
import com.example.mrdetective.data.local.entity.DialogueMessageEntity
import com.example.mrdetective.data.local.entity.InvestigationEntity
import com.example.mrdetective.data.local.entity.NoteEntity
import com.example.mrdetective.data.local.entity.PendingPhotoUploadEntity
import com.example.mrdetective.data.local.entity.PuzzleEntity
import com.example.mrdetective.data.local.entity.SolvedPuzzleEntity
import com.example.mrdetective.data.local.entity.SuspectEntity
import com.example.mrdetective.data.local.entity.SyncQueueEntity

/** Stores the local game cache, progress, and pending synchronization work in Room. */
@Database(
    entities = [
        CaseEntity::class, SuspectEntity::class, ClueEntity::class,
        InvestigationEntity::class, CollectedClueEntity::class, SolvedPuzzleEntity::class,
        DialogueMessageEntity::class, NoteEntity::class, PuzzleEntity::class, SyncQueueEntity::class,
        PendingPhotoUploadEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    /** Returns the DAO for cached cases. */
    abstract fun caseDao(): CaseDao

    /** Returns the DAO for cached suspect profiles. */
    abstract fun suspectDao(): SuspectDao

    /** Returns the DAO for cached clues and inventory entries. */
    abstract fun clueDao(): ClueDao

    /** Returns the DAO for local investigations and their progress. */
    abstract fun investigationDao(): InvestigationDao

    /** Returns the DAO for cached conversations. */
    abstract fun dialogueDao(): DialogueDao

    /** Returns the DAO for local investigation notes. */
    abstract fun noteDao(): NoteDao

    /** Returns the DAO for cached puzzle metadata. */
    abstract fun puzzleDao(): PuzzleDao

    /** Returns the DAO for pending synchronization operations. */
    abstract fun syncQueueDao(): SyncQueueDao

    /** Returns the DAO for pending clue photo uploads. */
    abstract fun pendingPhotoDao(): PendingPhotoDao

    /** Holds migrations for the local game database. */
    companion object {
        /** Version 1 is the initial local game cache; v2 adds durable sync/photo outboxes. */
        val MIGRATION_1_2 =
            object : Migration(1, 2) {
                override suspend fun migrate(db: SQLiteConnection) {
                    db.execSQL(
                        """CREATE TABLE IF NOT EXISTS sync_queue (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    entityType TEXT NOT NULL, entityId TEXT NOT NULL, operation TEXT NOT NULL,
                    payload TEXT NOT NULL, createdAt INTEGER NOT NULL, attempts INTEGER NOT NULL,
                    lastError TEXT)""",
                    )
                    db.execSQL(
                        """CREATE TABLE IF NOT EXISTS pending_photo_uploads (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    clueId INTEGER NOT NULL, localPath TEXT NOT NULL,
                    createdAt INTEGER NOT NULL, attempts INTEGER NOT NULL)""",
                    )
                }
            }
        val MIGRATION_2_3 =
            object : Migration(2, 3) {
                override suspend fun migrate(db: SQLiteConnection) {
                    db.execSQL(
                        """CREATE TABLE IF NOT EXISTS puzzles (
                    id INTEGER NOT NULL PRIMARY KEY, caseId INTEGER NOT NULL, kind TEXT NOT NULL,
                    title TEXT NOT NULL, hint TEXT, rewardClueId INTEGER)""",
                    )
                    db.execSQL("CREATE INDEX IF NOT EXISTS index_puzzles_caseId ON puzzles(caseId)")
                }
            }
    }
}
