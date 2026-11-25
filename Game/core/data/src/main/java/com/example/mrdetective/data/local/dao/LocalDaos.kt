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

/** Queries and updates cached case descriptions. */
@Dao
interface CaseDao {
    /** Observes the cached case catalog as it changes. */
    @Query("SELECT * FROM cases ORDER BY id")
    fun observeAll(): Flow<List<CaseEntity>>

    /** Reads the cached case matching the given identifier. */
    @Query("SELECT * FROM cases WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): CaseEntity?

    /** Returns the current snapshot of cached cases. */
    @Query("SELECT * FROM cases ORDER BY id")
    suspend fun getAll(): List<CaseEntity>

    /** Inserts or updates the supplied case records. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<CaseEntity>)

    /** Inserts or updates one cached case. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: CaseEntity)
}

/** Queries and updates cached suspect profiles. */
@Dao interface SuspectDao {
    /** Observes cached suspects for the selected case. */
    @Query("SELECT * FROM suspects WHERE caseId = :caseId ORDER BY id")
    fun observeByCase(caseId: Int): Flow<List<SuspectEntity>>

    /** Returns cached suspects assigned to the selected case. */
    @Query("SELECT * FROM suspects WHERE caseId = :caseId ORDER BY id")
    suspend fun getByCase(caseId: Int): List<SuspectEntity>

    /** Reads the cached suspect matching the given identifier. */
    @Query("SELECT * FROM suspects WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): SuspectEntity?

    /** Inserts or updates the supplied suspect profiles. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<SuspectEntity>)
}

/** Queries cached clue descriptions, imagery, and inventory entries. */
@Dao interface ClueDao {
    /** Reads one cached clue description. */
    @Query("SELECT * FROM clues WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): ClueEntity?

    /** Returns cached clues associated with the selected case. */
    @Query("SELECT * FROM clues WHERE caseId = :caseId ORDER BY id")
    suspend fun getByCase(caseId: Int): List<ClueEntity>

    /** Returns up to fifty cached clues with an image reference. */
    @Query("SELECT * FROM clues WHERE image IS NOT NULL ORDER BY id LIMIT 50")
    suspend fun getClueImages(): List<ClueEntity>

    /** Observes the clues collected in the specified investigation. */
    @Query(
        "SELECT c.* FROM clues c INNER JOIN collected_clues cc ON c.id = cc.clueId " +
            "WHERE cc.investigationId = :investigationId ORDER BY cc.collectedAt",
    )
    fun observeInventory(investigationId: Int): Flow<List<ClueEntity>>

    /** Returns the current snapshot of an investigation's collected clues. */
    @Query(
        "SELECT c.* FROM clues c INNER JOIN collected_clues cc ON c.id = cc.clueId " +
            "WHERE cc.investigationId = :investigationId ORDER BY cc.collectedAt",
    )
    suspend fun getInventory(investigationId: Int): List<ClueEntity>

    /** Inserts or updates the supplied clue records. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<ClueEntity>)
}

/** Stores local investigations and their collected clues and solved puzzles. */
@Dao interface InvestigationDao {
    /** Reads a local investigation by its record identifier. */
    @Query("SELECT * FROM investigations WHERE id = :id LIMIT 1")
    suspend fun getInvestigation(id: Int): InvestigationEntity?

    /** Returns an investigation associated with the specified case. */
    @Query("SELECT * FROM investigations WHERE caseId = :caseId ORDER BY updatedAt DESC LIMIT 1")
    suspend fun getByCase(caseId: Int): InvestigationEntity?

    /** Returns the specified player's investigation of the case. */
    @Query("SELECT * FROM investigations WHERE caseId = :caseId AND userId = :userId ORDER BY updatedAt DESC LIMIT 1")
    suspend fun getByCaseForUser(
        caseId: Int,
        userId: String,
    ): InvestigationEntity?

    /** Lists investigations belonging to the specified player. */
    @Query("SELECT * FROM investigations WHERE userId = :userId ORDER BY updatedAt DESC, id DESC")
    suspend fun getForUser(userId: String): List<InvestigationEntity>

    /** Lists the investigation records stored on this device. */
    @Query("SELECT * FROM investigations ORDER BY updatedAt DESC")
    suspend fun getAllInvestigations(): List<InvestigationEntity>

    /** Inserts or updates a local investigation record. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertInvestigation(item: InvestigationEntity)

    /** Inserts the link between an investigation and a collected clue. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun collectClue(item: CollectedClueEntity): Long

    /** Inserts the link between an investigation and a solved puzzle. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun markPuzzleSolved(item: SolvedPuzzleEntity): Long

    /** Lists the identifiers of clues already collected in the investigation. */
    @Query("SELECT clueId FROM collected_clues WHERE investigationId = :investigationId ORDER BY collectedAt")
    suspend fun collectedClueIds(investigationId: Int): List<Int>

    /** Observes the identifiers of clues earned in the selected investigation. */
    @Query(
        """
        SELECT DISTINCT captured.clueId FROM collected_clues AS captured
        INNER JOIN investigations AS investigation ON investigation.id = captured.investigationId
        WHERE investigation.caseId = :caseId AND investigation.userId = :userId
    """,
    )
    fun observeCollectedClueIds(
        caseId: Int,
        userId: String,
    ): Flow<List<Int>>

    /** Lists the identifiers of puzzles already solved in the investigation. */
    @Query("SELECT puzzleId FROM solved_puzzles WHERE investigationId = :investigationId")
    suspend fun solvedPuzzleIds(investigationId: Int): List<Int>

    /** Moves collected clue links to the synchronized investigation identifier. */
    @Query("UPDATE collected_clues SET investigationId = :newId WHERE investigationId = :oldId")
    suspend fun remapCollected(
        oldId: Int,
        newId: Int,
    )

    /** Moves solved puzzle links to the synchronized investigation identifier. */
    @Query("UPDATE solved_puzzles SET investigationId = :newId WHERE investigationId = :oldId")
    suspend fun remapSolved(
        oldId: Int,
        newId: Int,
    )

    /** Deletes the local investigation record matching the identifier. */
    @Query("DELETE FROM investigations WHERE id = :id")
    suspend fun deleteInvestigation(id: Int)

    /** Changes the identifier of a locally created investigation after sync. */
    @Query("UPDATE investigations SET id = :newId WHERE id = :oldId")
    suspend fun remapInvestigation(
        oldId: Int,
        newId: Int,
    )
}

/** Stores conversation history and provisional messages for synchronization. */
@Dao interface DialogueDao {
    /** Observes locally stored messages for a case and suspect. */
    @Query("SELECT * FROM dialogue_messages WHERE caseId = :caseId AND suspectId = :suspectId ORDER BY createdAt, id")
    fun observeHistory(
        caseId: Int,
        suspectId: Int,
    ): Flow<List<DialogueMessageEntity>>

    /** Returns locally stored messages for a case and suspect. */
    @Query("SELECT * FROM dialogue_messages WHERE caseId = :caseId AND suspectId = :suspectId ORDER BY createdAt, id")
    suspend fun getHistory(
        caseId: Int,
        suspectId: Int,
    ): List<DialogueMessageEntity>

    /** Returns the history for the selected suspect in an investigation. */
    @Query("SELECT * FROM dialogue_messages WHERE investigationId = :investigationId AND suspectId = :suspectId ORDER BY createdAt, id")
    suspend fun getInvestigationHistory(
        investigationId: Int,
        suspectId: Int,
    ): List<DialogueMessageEntity>

    /** Removes temporary local messages when the confirmed history is available. */
    @Query(
        "DELETE FROM dialogue_messages WHERE investigationId = :investigationId AND suspectId = :suspectId AND id IN (:ids) AND synced = 1",
    )
    suspend fun removeProvisionalMessages(
        investigationId: Int,
        suspectId: Int,
        ids: List<Long>,
    )

    /** Inserts or replaces the supplied message. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: DialogueMessageEntity)

    /** Reads one locally stored dialogue message. */
    @Query("SELECT * FROM dialogue_messages WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): DialogueMessageEntity?

    /** Deletes the stored message matching the given identifier. */
    @Query("DELETE FROM dialogue_messages WHERE id = :id")
    suspend fun delete(id: Long)

    /** Moves dialogue messages from a provisional investigation to its synchronized identifier. */
    @Query("UPDATE dialogue_messages SET investigationId = :newId WHERE investigationId = :oldId")
    suspend fun remapInvestigation(
        oldId: Int,
        newId: Int,
    )
}

/** Stores investigation notes and tracks whether their changes have been synchronized. */
@Dao interface NoteDao {
    /** Returns the latest note for the selected investigation. */
    @Query("SELECT * FROM notes WHERE investigationId = :investigationId ORDER BY localUpdatedAt DESC LIMIT 1")
    suspend fun latest(investigationId: Int): NoteEntity?

    /** Observes the notes for an investigation, starting with the most recently updated. */
    @Query("SELECT * FROM notes WHERE investigationId = :investigationId ORDER BY localUpdatedAt DESC")
    fun observe(investigationId: Int): Flow<List<NoteEntity>>

    /** Inserts or updates a note in Room. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: NoteEntity)

    /** Deletes the stored note matching the given identifier. */
    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun delete(id: Int)

    /** Lists local notes that still need synchronization. */
    @Query("SELECT * FROM notes WHERE synced = 0 ORDER BY localUpdatedAt")
    suspend fun pending(): List<NoteEntity>

    /** Marks the note as synchronized with the server. */
    @Query("UPDATE notes SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: Int)

    /** Updates notes to reference the synchronized investigation identifier. */
    @Query("UPDATE notes SET investigationId = :newId WHERE investigationId = :oldId")
    suspend fun remapInvestigation(
        oldId: Int,
        newId: Int,
    )

    /** Replaces the local note identifier with the server identifier and marks the note as synchronized. */
    @Query("UPDATE notes SET id = :newId, synced = 1 WHERE id = :oldId")
    suspend fun remapNote(
        oldId: Int,
        newId: Int,
    )
}

/** Maintains pending operations, retry details, and camera submissions. */
@Dao interface SyncQueueDao {
    /** Returns queued operations waiting for a synchronization attempt. */
    @Query("SELECT * FROM sync_queue ORDER BY createdAt, id")
    suspend fun pending(): List<SyncQueueEntity>

    /** Adds an operation to the persistent synchronization queue. */
    @Insert suspend fun enqueue(item: SyncQueueEntity): Long

    /** Removes the completed operation from the queue. */
    @Query("DELETE FROM sync_queue WHERE id = :id")
    suspend fun remove(id: Long)

    /** Records an attempt and its error for a queued operation. */
    @Query("UPDATE sync_queue SET attempts = attempts + 1, lastError = :error WHERE id = :id")
    suspend fun recordFailure(
        id: Long,
        error: String,
    )

    /** Observes the number of pending operations. */
    @Query("SELECT COUNT(*) FROM sync_queue")
    fun observeCount(): Flow<Int>

    /** Observes a queued camera capture for the specified investigation and puzzle. */
    @Query(
        """
        SELECT CASE
            WHEN EXISTS (SELECT 1 FROM solved_puzzles WHERE investigationId = :investigationId AND puzzleId = :puzzleId) THEN 2
            WHEN EXISTS (SELECT 1 FROM sync_queue WHERE entityType = 'camera_capture' AND entityId = :key) THEN 1
            ELSE 0
        END
    """,
    )
    fun observeCameraCapture(
        investigationId: Int,
        puzzleId: Int,
        key: String,
    ): Flow<Int>

    /** Observes the last error for a queued camera capture. */
    @Query("SELECT lastError FROM sync_queue WHERE entityType = 'camera_capture' AND entityId = :key ORDER BY id DESC LIMIT 1")
    fun observeCameraCaptureError(key: String): Flow<String?>

    /** Updates the serialized request for the queued operation. */
    @Query("UPDATE sync_queue SET payload = replace(payload, :oldValue, :newValue) WHERE payload LIKE '%' || :oldValue || '%'")
    suspend fun replacePayload(
        oldValue: String,
        newValue: String,
    )
}

/** Maintains the outbox of locally stored clue photos. */
@Dao interface PendingPhotoDao {
    /** Returns the photos still waiting to be uploaded. */
    @Query("SELECT * FROM pending_photo_uploads ORDER BY createdAt")
    suspend fun pending(): List<PendingPhotoUploadEntity>

    /** Adds a clue photo to the upload outbox. */
    @Insert suspend fun enqueue(item: PendingPhotoUploadEntity): Long

    /** Removes a completed photo upload from the outbox. */
    @Query("DELETE FROM pending_photo_uploads WHERE id = :id")
    suspend fun remove(id: Long)

    /** Increments the recorded retry count for a photo upload. */
    @Query("UPDATE pending_photo_uploads SET attempts = attempts + 1 WHERE id = :id")
    suspend fun incrementAttempts(id: Long)
}

/** Queries and updates cached puzzle metadata. */
@Dao interface PuzzleDao {
    /** Returns the cached puzzle metadata for the identifier. */
    @Query("SELECT * FROM puzzles WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): PuzzleEntity?

    /** Returns the cached puzzle metadata for the selected case. */
    @Query("SELECT * FROM puzzles WHERE caseId = :caseId ORDER BY id")
    suspend fun getByCase(caseId: Int): List<PuzzleEntity>

    /** Inserts or updates the supplied puzzle records. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<PuzzleEntity>)
}
