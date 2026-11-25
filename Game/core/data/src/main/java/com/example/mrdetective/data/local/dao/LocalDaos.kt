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

/** Читает и обновляет описания дел в кеше. */
@Dao
interface CaseDao {
    /** Отслеживает изменения списка дел в кеше. */
    @Query("SELECT * FROM cases ORDER BY id")
    fun observeAll(): Flow<List<CaseEntity>>

    /** Читает кешированное дело по идентификатору. */
    @Query("SELECT * FROM cases WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): CaseEntity?

    /** Возвращает текущий список дел из кеша. */
    @Query("SELECT * FROM cases ORDER BY id")
    suspend fun getAll(): List<CaseEntity>

    /** Добавляет или обновляет переданные записи дел. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<CaseEntity>)

    /** Добавляет или обновляет запись дела в кеше. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: CaseEntity)
}

/** Читает и обновляет профили подозреваемых в кеше. */
@Dao interface SuspectDao {
    /** Отслеживает изменения списка подозреваемых выбранного дела в кеше. */
    @Query("SELECT * FROM suspects WHERE caseId = :caseId ORDER BY id")
    fun observeByCase(caseId: Int): Flow<List<SuspectEntity>>

    /** Возвращает подозреваемых выбранного дела из кеша. */
    @Query("SELECT * FROM suspects WHERE caseId = :caseId ORDER BY id")
    suspend fun getByCase(caseId: Int): List<SuspectEntity>

    /** Читает профиль подозреваемого из кеша по идентификатору. */
    @Query("SELECT * FROM suspects WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): SuspectEntity?

    /** Добавляет или обновляет переданные профили подозреваемых. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<SuspectEntity>)
}

/** Читает из кеша описания, изображения улик и записи инвентаря. */
@Dao interface ClueDao {
    /** Читает описание одной улики из кеша. */
    @Query("SELECT * FROM clues WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): ClueEntity?

    /** Возвращает сохранённые в кеше улики выбранного дела. */
    @Query("SELECT * FROM clues WHERE caseId = :caseId ORDER BY id")
    suspend fun getByCase(caseId: Int): List<ClueEntity>

    /** Возвращает до пятидесяти кешированных улик со ссылкой на изображение. */
    @Query("SELECT * FROM clues WHERE image IS NOT NULL ORDER BY id LIMIT 50")
    suspend fun getClueImages(): List<ClueEntity>

    /** Отслеживает изменения списка найденных улик расследования. */
    @Query(
        "SELECT c.* FROM clues c INNER JOIN collected_clues cc ON c.id = cc.clueId " +
            "WHERE cc.investigationId = :investigationId ORDER BY cc.collectedAt",
    )
    fun observeInventory(investigationId: Int): Flow<List<ClueEntity>>

    /** Возвращает текущий список найденных улик расследования. */
    @Query(
        "SELECT c.* FROM clues c INNER JOIN collected_clues cc ON c.id = cc.clueId " +
            "WHERE cc.investigationId = :investigationId ORDER BY cc.collectedAt",
    )
    suspend fun getInventory(investigationId: Int): List<ClueEntity>

    /** Добавляет или обновляет переданные записи улик. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<ClueEntity>)
}

/** Хранит локальные расследования, найденные улики и решённые головоломки. */
@Dao interface InvestigationDao {
    /** Читает локальное расследование по идентификатору записи. */
    @Query("SELECT * FROM investigations WHERE id = :id LIMIT 1")
    suspend fun getInvestigation(id: Int): InvestigationEntity?

    /** Возвращает расследование, связанное с указанным делом. */
    @Query("SELECT * FROM investigations WHERE caseId = :caseId ORDER BY updatedAt DESC LIMIT 1")
    suspend fun getByCase(caseId: Int): InvestigationEntity?

    /** Возвращает расследование указанного игрока по делу. */
    @Query("SELECT * FROM investigations WHERE caseId = :caseId AND userId = :userId ORDER BY updatedAt DESC LIMIT 1")
    suspend fun getByCaseForUser(
        caseId: Int,
        userId: String,
    ): InvestigationEntity?

    /** Возвращает список расследований указанного игрока. */
    @Query("SELECT * FROM investigations WHERE userId = :userId ORDER BY updatedAt DESC, id DESC")
    suspend fun getForUser(userId: String): List<InvestigationEntity>

    /** Возвращает расследования, сохранённые на устройстве. */
    @Query("SELECT * FROM investigations ORDER BY updatedAt DESC")
    suspend fun getAllInvestigations(): List<InvestigationEntity>

    /** Добавляет или обновляет локальную запись расследования. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertInvestigation(item: InvestigationEntity)

    /** Добавляет связь расследования с найденной уликой. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun collectClue(item: CollectedClueEntity): Long

    /** Добавляет связь расследования с решённой головоломкой. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun markPuzzleSolved(item: SolvedPuzzleEntity): Long

    /** Возвращает идентификаторы найденных улик расследования. */
    @Query("SELECT clueId FROM collected_clues WHERE investigationId = :investigationId ORDER BY collectedAt")
    suspend fun collectedClueIds(investigationId: Int): List<Int>

    /** Отслеживает изменения списка идентификаторов улик, найденных в расследовании. */
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

    /** Возвращает идентификаторы решённых головоломок расследования. */
    @Query("SELECT puzzleId FROM solved_puzzles WHERE investigationId = :investigationId")
    suspend fun solvedPuzzleIds(investigationId: Int): List<Int>

    /** Привязывает найденные улики к серверному идентификатору расследования. */
    @Query("UPDATE collected_clues SET investigationId = :newId WHERE investigationId = :oldId")
    suspend fun remapCollected(
        oldId: Int,
        newId: Int,
    )

    /** Привязывает решённые головоломки к серверному идентификатору расследования. */
    @Query("UPDATE solved_puzzles SET investigationId = :newId WHERE investigationId = :oldId")
    suspend fun remapSolved(
        oldId: Int,
        newId: Int,
    )

    /** Удаляет локальную запись расследования по идентификатору. */
    @Query("DELETE FROM investigations WHERE id = :id")
    suspend fun deleteInvestigation(id: Int)

    /** Заменяет идентификатор локально созданного расследования после синхронизации. */
    @Query("UPDATE investigations SET id = :newId WHERE id = :oldId")
    suspend fun remapInvestigation(
        oldId: Int,
        newId: Int,
    )
}

/** Хранит историю диалогов и временные сообщения до синхронизации. */
@Dao interface DialogueDao {
    /** Отслеживает локальные сообщения для пары «дело и подозреваемый». */
    @Query("SELECT * FROM dialogue_messages WHERE caseId = :caseId AND suspectId = :suspectId ORDER BY createdAt, id")
    fun observeHistory(
        caseId: Int,
        suspectId: Int,
    ): Flow<List<DialogueMessageEntity>>

    /** Возвращает локальные сообщения для указанного дела и подозреваемого. */
    @Query("SELECT * FROM dialogue_messages WHERE caseId = :caseId AND suspectId = :suspectId ORDER BY createdAt, id")
    suspend fun getHistory(
        caseId: Int,
        suspectId: Int,
    ): List<DialogueMessageEntity>

    /** Возвращает историю диалога с выбранным подозреваемым в расследовании. */
    @Query("SELECT * FROM dialogue_messages WHERE investigationId = :investigationId AND suspectId = :suspectId ORDER BY createdAt, id")
    suspend fun getInvestigationHistory(
        investigationId: Int,
        suspectId: Int,
    ): List<DialogueMessageEntity>

    /** Удаляет временные сообщения, когда доступна подтверждённая история. */
    @Query(
        "DELETE FROM dialogue_messages WHERE investigationId = :investigationId AND suspectId = :suspectId AND id IN (:ids) AND synced = 1",
    )
    suspend fun removeProvisionalMessages(
        investigationId: Int,
        suspectId: Int,
        ids: List<Long>,
    )

    /** Добавляет или заменяет переданное сообщение. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: DialogueMessageEntity)

    /** Читает одно локально сохранённое сообщение. */
    @Query("SELECT * FROM dialogue_messages WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): DialogueMessageEntity?

    /** Удаляет сохранённое сообщение по идентификатору. */
    @Query("DELETE FROM dialogue_messages WHERE id = :id")
    suspend fun delete(id: Long)

    /** Переносит сообщения с временного идентификатора расследования на серверный. */
    @Query("UPDATE dialogue_messages SET investigationId = :newId WHERE investigationId = :oldId")
    suspend fun remapInvestigation(
        oldId: Int,
        newId: Int,
    )
}

/** Хранит заметки расследования и отмечает, синхронизированы ли их изменения. */
@Dao interface NoteDao {
    /** Возвращает последнюю заметку выбранного расследования. */
    @Query("SELECT * FROM notes WHERE investigationId = :investigationId ORDER BY localUpdatedAt DESC LIMIT 1")
    suspend fun latest(investigationId: Int): NoteEntity?

    /** Отслеживает заметки расследования в порядке от последнего изменения. */
    @Query("SELECT * FROM notes WHERE investigationId = :investigationId ORDER BY localUpdatedAt DESC")
    fun observe(investigationId: Int): Flow<List<NoteEntity>>

    /** Добавляет или обновляет заметку в Room. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: NoteEntity)

    /** Удаляет сохранённую заметку по идентификатору. */
    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun delete(id: Int)

    /** Возвращает локальные заметки, ожидающие синхронизации. */
    @Query("SELECT * FROM notes WHERE synced = 0 ORDER BY localUpdatedAt")
    suspend fun pending(): List<NoteEntity>

    /** Помечает заметку как синхронизированную с сервером. */
    @Query("UPDATE notes SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: Int)

    /** Перепривязывает заметки к серверному идентификатору расследования. */
    @Query("UPDATE notes SET investigationId = :newId WHERE investigationId = :oldId")
    suspend fun remapInvestigation(
        oldId: Int,
        newId: Int,
    )

    /** Заменяет локальный идентификатор заметки серверным и отмечает её синхронизированной. */
    @Query("UPDATE notes SET id = :newId, synced = 1 WHERE id = :oldId")
    suspend fun remapNote(
        oldId: Int,
        newId: Int,
    )
}

/** Хранит очередь операций, сведения о повторах и отправленные снимки улик. */
@Dao interface SyncQueueDao {
    /** Возвращает операции, ожидающие попытки синхронизации. */
    @Query("SELECT * FROM sync_queue ORDER BY createdAt, id")
    suspend fun pending(): List<SyncQueueEntity>

    /** Добавляет операцию в сохраняемую очередь синхронизации. */
    @Insert suspend fun enqueue(item: SyncQueueEntity): Long

    /** Удаляет выполненную операцию из очереди. */
    @Query("DELETE FROM sync_queue WHERE id = :id")
    suspend fun remove(id: Long)

    /** Записывает попытку и её ошибку для операции в очереди. */
    @Query("UPDATE sync_queue SET attempts = attempts + 1, lastError = :error WHERE id = :id")
    suspend fun recordFailure(
        id: Long,
        error: String,
    )

    /** Отслеживает количество операций в очереди. */
    @Query("SELECT COUNT(*) FROM sync_queue")
    fun observeCount(): Flow<Int>

    /** Отслеживает состояние снимка улики для указанного расследования и головоломки. */
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

    /** Отслеживает последнюю ошибку снимка улики в очереди. */
    @Query("SELECT lastError FROM sync_queue WHERE entityType = 'camera_capture' AND entityId = :key ORDER BY id DESC LIMIT 1")
    fun observeCameraCaptureError(key: String): Flow<String?>

    /** Обновляет сериализованный запрос операции в очереди. */
    @Query("UPDATE sync_queue SET payload = replace(payload, :oldValue, :newValue) WHERE payload LIKE '%' || :oldValue || '%'")
    suspend fun replacePayload(
        oldValue: String,
        newValue: String,
    )
}

/** Хранит очередь отправки локально сохранённых фотографий улик. */
@Dao interface PendingPhotoDao {
    /** Возвращает фотографии, ожидающие загрузки. */
    @Query("SELECT * FROM pending_photo_uploads ORDER BY createdAt")
    suspend fun pending(): List<PendingPhotoUploadEntity>

    /** Добавляет фотографию улики в очередь загрузки. */
    @Insert suspend fun enqueue(item: PendingPhotoUploadEntity): Long

    /** Удаляет из очереди запись о загруженной фотографии. */
    @Query("DELETE FROM pending_photo_uploads WHERE id = :id")
    suspend fun remove(id: Long)

    /** Увеличивает счётчик попыток загрузки фотографии. */
    @Query("UPDATE pending_photo_uploads SET attempts = attempts + 1 WHERE id = :id")
    suspend fun incrementAttempts(id: Long)
}

/** Читает и обновляет метаданные головоломок в кеше. */
@Dao interface PuzzleDao {
    /** Возвращает метаданные головоломки из кеша по идентификатору. */
    @Query("SELECT * FROM puzzles WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): PuzzleEntity?

    /** Возвращает метаданные головоломок выбранного дела из кеша. */
    @Query("SELECT * FROM puzzles WHERE caseId = :caseId ORDER BY id")
    suspend fun getByCase(caseId: Int): List<PuzzleEntity>

    /** Добавляет или обновляет переданные записи головоломок. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<PuzzleEntity>)
}
