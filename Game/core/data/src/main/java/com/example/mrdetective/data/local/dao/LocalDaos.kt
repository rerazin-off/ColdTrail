package com.example.mrdetective.data.local.dao


import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
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
import kotlinx.coroutines.flow.Flow

@Dao
interface CaseDao {
    @Query("SELECT * FROM cases ORDER BY id") fun observeAll(): Flow<List<CaseEntity>>
    @Query("SELECT * FROM cases WHERE id = :id LIMIT 1") suspend fun getById(id: Int): CaseEntity?
    @Query("SELECT * FROM cases ORDER BY id") suspend fun getAll(): List<CaseEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertAll(items: List<CaseEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(item: CaseEntity)
}

@Dao interface SuspectDao {
    @Query("SELECT * FROM suspects WHERE caseId = :caseId ORDER BY id") fun observeByCase(caseId: Int): Flow<List<SuspectEntity>>
    @Query("SELECT * FROM suspects WHERE caseId = :caseId ORDER BY id") suspend fun getByCase(caseId: Int): List<SuspectEntity>
    @Query("SELECT * FROM suspects WHERE id = :id LIMIT 1") suspend fun getById(id: Int): SuspectEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertAll(items: List<SuspectEntity>)
}

@Dao interface ClueDao {
    @Query("SELECT * FROM clues WHERE id = :id LIMIT 1") suspend fun getById(id: Int): ClueEntity?
    @Query("SELECT * FROM clues WHERE caseId = :caseId ORDER BY id") suspend fun getByCase(caseId: Int): List<ClueEntity>
    @Query("SELECT c.* FROM clues c INNER JOIN collected_clues cc ON c.id = cc.clueId WHERE cc.investigationId = :investigationId ORDER BY cc.collectedAt")
    fun observeInventory(investigationId: Int): Flow<List<ClueEntity>>
    @Query("SELECT c.* FROM clues c INNER JOIN collected_clues cc ON c.id = cc.clueId WHERE cc.investigationId = :investigationId ORDER BY cc.collectedAt")
    suspend fun getInventory(investigationId: Int): List<ClueEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertAll(items: List<ClueEntity>)
}

@Dao interface InvestigationDao {
    @Query("SELECT * FROM investigations WHERE id = :id LIMIT 1") suspend fun getInvestigation(id: Int): InvestigationEntity?
    @Query("SELECT * FROM investigations WHERE caseId = :caseId ORDER BY updatedAt DESC LIMIT 1") suspend fun getByCase(caseId: Int): InvestigationEntity?
    @Query("SELECT * FROM investigations WHERE caseId = :caseId AND userId = :userId ORDER BY updatedAt DESC LIMIT 1")
    suspend fun getByCaseForUser(caseId: Int, userId: String): InvestigationEntity?

    @Query("SELECT * FROM investigations WHERE userId = :userId ORDER BY updatedAt DESC, id DESC")
    suspend fun getForUser(userId: String): List<InvestigationEntity>

    @Query("SELECT * FROM investigations ORDER BY updatedAt DESC") suspend fun getAllInvestigations(): List<InvestigationEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertInvestigation(item: InvestigationEntity)
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun collectClue(item: CollectedClueEntity): Long
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun markPuzzleSolved(item: SolvedPuzzleEntity): Long
    @Query("SELECT clueId FROM collected_clues WHERE investigationId = :investigationId ORDER BY collectedAt") suspend fun collectedClueIds(investigationId: Int): List<Int>
    @Query("SELECT puzzleId FROM solved_puzzles WHERE investigationId = :investigationId") suspend fun solvedPuzzleIds(investigationId: Int): List<Int>

    @Query("UPDATE collected_clues SET investigationId = :newId WHERE investigationId = :oldId") suspend fun remapCollected(oldId: Int, newId: Int)
    @Query("UPDATE solved_puzzles SET investigationId = :newId WHERE investigationId = :oldId") suspend fun remapSolved(oldId: Int, newId: Int)
    @Query("DELETE FROM investigations WHERE id = :id") suspend fun deleteInvestigation(id: Int)
    @Query("UPDATE investigations SET id = :newId WHERE id = :oldId") suspend fun remapInvestigation(oldId: Int, newId: Int)
}

@Dao interface DialogueDao {
    @Query("SELECT * FROM dialogue_messages WHERE caseId = :caseId AND suspectId = :suspectId ORDER BY createdAt, id")
    fun observeHistory(caseId: Int, suspectId: Int): Flow<List<DialogueMessageEntity>>
    @Query("SELECT * FROM dialogue_messages WHERE caseId = :caseId AND suspectId = :suspectId ORDER BY createdAt, id")
    suspend fun getHistory(caseId: Int, suspectId: Int): List<DialogueMessageEntity>

    @Query("SELECT * FROM dialogue_messages WHERE investigationId = :investigationId AND suspectId = :suspectId ORDER BY createdAt, id")
    suspend fun getInvestigationHistory(investigationId: Int, suspectId: Int): List<DialogueMessageEntity>

    @Query("DELETE FROM dialogue_messages WHERE investigationId = :investigationId AND suspectId = :suspectId AND id IN (:ids) AND synced = 1")
    suspend fun removeProvisionalMessages(investigationId: Int, suspectId: Int, ids: List<Long>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(item: DialogueMessageEntity)
    @Query("SELECT * FROM dialogue_messages WHERE id = :id LIMIT 1") suspend fun getById(id: Long): DialogueMessageEntity?
    @Query("DELETE FROM dialogue_messages WHERE id = :id")
    suspend fun delete(id: Long)
    @Query("UPDATE dialogue_messages SET investigationId = :newId WHERE investigationId = :oldId") suspend fun remapInvestigation(oldId: Int, newId: Int)
}

@Dao interface NoteDao {
    @Query("SELECT * FROM notes WHERE investigationId = :investigationId ORDER BY localUpdatedAt DESC LIMIT 1")
    suspend fun latest(investigationId: Int): NoteEntity?
    @Query("SELECT * FROM notes WHERE investigationId = :investigationId ORDER BY localUpdatedAt DESC")
    fun observe(investigationId: Int): Flow<List<NoteEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(item: NoteEntity)
    @Query("DELETE FROM notes WHERE id = :id") suspend fun delete(id: Int)
    @Query("SELECT * FROM notes WHERE synced = 0 ORDER BY localUpdatedAt") suspend fun pending(): List<NoteEntity>
    @Query("UPDATE notes SET synced = 1 WHERE id = :id") suspend fun markSynced(id: Int)
    @Query("UPDATE notes SET investigationId = :newId WHERE investigationId = :oldId") suspend fun remapInvestigation(oldId: Int, newId: Int)
    @Query("UPDATE notes SET id = :newId, synced = 1 WHERE id = :oldId") suspend fun remapNote(oldId: Int, newId: Int)
}

@Dao interface SyncQueueDao {
    @Query("SELECT * FROM sync_queue ORDER BY createdAt, id") suspend fun pending(): List<SyncQueueEntity>
    @Insert suspend fun enqueue(item: SyncQueueEntity): Long
    @Query("DELETE FROM sync_queue WHERE id = :id") suspend fun remove(id: Long)
    @Query("UPDATE sync_queue SET attempts = attempts + 1, lastError = :error WHERE id = :id") suspend fun recordFailure(id: Long, error: String)
    @Query("SELECT COUNT(*) FROM sync_queue") fun observeCount(): Flow<Int>
    @Query("""
        SELECT CASE
            WHEN EXISTS (SELECT 1 FROM solved_puzzles WHERE investigationId = :investigationId AND puzzleId = :puzzleId) THEN 2
            WHEN EXISTS (SELECT 1 FROM sync_queue WHERE entityType = 'camera_capture' AND entityId = :key) THEN 1
            ELSE 0
        END
    """)
    fun observeCameraCapture(investigationId: Int, puzzleId: Int, key: String): Flow<Int>
    @Query("UPDATE sync_queue SET payload = replace(payload, :oldValue, :newValue) WHERE payload LIKE '%' || :oldValue || '%'")
    suspend fun replacePayload(oldValue: String, newValue: String)
}

@Dao interface PendingPhotoDao {
    @Query("SELECT * FROM pending_photo_uploads ORDER BY createdAt") suspend fun pending(): List<PendingPhotoUploadEntity>
    @Insert suspend fun enqueue(item: PendingPhotoUploadEntity): Long
    @Query("DELETE FROM pending_photo_uploads WHERE id = :id") suspend fun remove(id: Long)
    @Query("UPDATE pending_photo_uploads SET attempts = attempts + 1 WHERE id = :id") suspend fun incrementAttempts(id: Long)
}


@Dao interface PuzzleDao {
    @Query("SELECT * FROM puzzles WHERE id = :id LIMIT 1") suspend fun getById(id: Int): PuzzleEntity?
    @Query("SELECT * FROM puzzles WHERE caseId = :caseId ORDER BY id") suspend fun getByCase(caseId: Int): List<PuzzleEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertAll(items: List<PuzzleEntity>)
}
