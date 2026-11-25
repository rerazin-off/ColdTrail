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

/** Хранит кеш игры, прогресс и очередь синхронизации в Room. */
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
    /** Возвращает DAO для кеша дел. */
    abstract fun caseDao(): CaseDao

    /** Возвращает DAO для кеша профилей подозреваемых. */
    abstract fun suspectDao(): SuspectDao

    /** Возвращает DAO для кеша улик и записей инвентаря. */
    abstract fun clueDao(): ClueDao

    /** Возвращает DAO для локальных расследований и их прогресса. */
    abstract fun investigationDao(): InvestigationDao

    /** Возвращает DAO для кеша диалогов. */
    abstract fun dialogueDao(): DialogueDao

    /** Возвращает DAO для локальных заметок расследования. */
    abstract fun noteDao(): NoteDao

    /** Возвращает DAO для кеша метаданных головоломок. */
    abstract fun puzzleDao(): PuzzleDao

    /** Возвращает DAO для операций, ожидающих синхронизации. */
    abstract fun syncQueueDao(): SyncQueueDao

    /** Возвращает DAO для очереди загрузки фотографий улик. */
    abstract fun pendingPhotoDao(): PendingPhotoDao

    /** Хранит миграции локальной базы игры. */
    companion object {
        /**
         * Первая версия содержит исходный кеш игры. Вторая добавляет сохраняемые очереди синхронизации и
         * загрузки фото.
         */
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
