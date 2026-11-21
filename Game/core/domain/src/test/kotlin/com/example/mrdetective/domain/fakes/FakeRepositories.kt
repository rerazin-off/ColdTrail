package com.example.mrdetective.domain.fakes

import com.example.mrdetective.domain.models.Accusation
import com.example.mrdetective.domain.models.Case
import com.example.mrdetective.domain.models.Clue
import com.example.mrdetective.domain.models.CollectedClue
import com.example.mrdetective.domain.models.DialogueMessage
import com.example.mrdetective.domain.models.InvestigationProgress
import com.example.mrdetective.domain.models.Note
import com.example.mrdetective.domain.models.Puzzle
import com.example.mrdetective.domain.models.Suspect
import com.example.mrdetective.domain.repositories.AccusationRepository
import com.example.mrdetective.domain.repositories.AuthRepository
import com.example.mrdetective.domain.repositories.CaseRepository
import com.example.mrdetective.domain.repositories.ClueRepository
import com.example.mrdetective.domain.repositories.DialogueRepository
import com.example.mrdetective.domain.repositories.InventoryRepository
import com.example.mrdetective.domain.repositories.NotebookRepository
import com.example.mrdetective.domain.repositories.PhotoRepository
import com.example.mrdetective.domain.repositories.ProgressRepository
import com.example.mrdetective.domain.repositories.PuzzleRepository
import com.example.mrdetective.domain.repositories.SceneRepository
import com.example.mrdetective.domain.repositories.SuspectRepository
import com.example.mrdetective.domain.hardware.MorseSignalPlayer

class FakeAuthRepository : AuthRepository {
    var signInResult: Result<String> = Result.success("user-1")
    var signUpResult: Result<String> = Result.success("user-1")
    val signInCalls = mutableListOf<List<Any?>>()
    val signUpCalls = mutableListOf<List<Any?>>()

    override suspend fun signIn(
        email: String,
        password: String,
    ): Result<String> {
        signInCalls += listOf(email, password)
        return signInResult
    }

    override suspend fun signUp(
        email: String,
        password: String,
    ): Result<String> {
        signUpCalls += listOf(email, password)
        return signUpResult
    }

    override suspend fun signOut() = Unit

    override suspend fun currentUser(): String? = null
}

class FakeAccusationRepository : AccusationRepository {
    var result: Result<Accusation> = Result.success(TestData.accusation)
    val calls = mutableListOf<List<Any?>>()

    override suspend fun getResult(caseId: Int): Result<Accusation> = Result.success(TestData.accusation)

    override suspend fun submitAccusation(
        caseId: Int,
        suspectId: Int,
        clueIds: List<Int>,
    ): Result<Accusation> {
        calls += listOf(caseId, suspectId, clueIds)
        return result
    }
}

class FakeCaseRepository : CaseRepository {
    var availableResult: Result<List<Case>> = Result.success(listOf(TestData.case))
    var startResult: Result<Unit> = Result.success(Unit)
    var availableCalls = 0
    val startCalls = mutableListOf<Int>()

    override suspend fun getAvailableCases(): Result<List<Case>> {
        availableCalls++
        return availableResult
    }

    override suspend fun getCase(id: Int): Result<Case> = Result.success(TestData.case)

    override suspend fun startCase(id: Int): Result<Unit> {
        startCalls += id
        return startResult
    }
}

class FakeClueRepository : ClueRepository {
    var clueResult: Result<Clue> = Result.success(TestData.clue)
    var collectResult: Result<Unit> = Result.success(Unit)
    val getClueCalls = mutableListOf<Int>()
    val collectCalls = mutableListOf<Int>()

    override suspend fun getClue(id: Int): Result<Clue> {
        getClueCalls += id
        return clueResult
    }

    override suspend fun getCollectedClues(caseId: Int): Result<List<Clue>> = Result.success(emptyList())

    override suspend fun collectClue(clueId: Int): Result<Unit> {
        collectCalls += clueId
        return collectResult
    }
}

class FakeDialogueRepository : DialogueRepository {
    var historyResult: Result<List<DialogueMessage>> = Result.success(listOf(TestData.message))
    var sendResult: Result<DialogueMessage> = Result.success(TestData.message)
    var presentResult: Result<DialogueMessage> = Result.success(TestData.message)
    val historyCalls = mutableListOf<List<Any?>>()
    val sendCalls = mutableListOf<List<Any?>>()
    val presentCalls = mutableListOf<List<Any?>>()

    override suspend fun getHistory(
        caseId: Int,
        suspectId: Int,
    ): Result<List<DialogueMessage>> {
        historyCalls += listOf(caseId, suspectId)
        return historyResult
    }

    override suspend fun sendMessage(
        caseId: Int,
        suspectId: Int,
        message: String,
    ): Result<DialogueMessage> {
        sendCalls += listOf(caseId, suspectId, message)
        return sendResult
    }

    override suspend fun presentClue(
        caseId: Int,
        suspectId: Int,
        clueId: Int,
        message: String,
    ): Result<DialogueMessage> {
        presentCalls += listOf(caseId, suspectId, clueId)
        return presentResult
    }
}

class FakeInventoryRepository : InventoryRepository {
    var inventoryResult: Result<List<Clue>> = Result.success(listOf(TestData.clue))
    var addResult: Result<CollectedClue> = Result.success(TestData.collectedClue)
    val inventoryCalls = mutableListOf<Int>()
    val addCalls = mutableListOf<List<Any?>>()

    override suspend fun getInventory(investigationId: Int): Result<List<Clue>> {
        inventoryCalls += investigationId
        return inventoryResult
    }

    override suspend fun addItem(
        investigationId: Int,
        clueId: Int,
    ): Result<CollectedClue> {
        addCalls += listOf(investigationId, clueId)
        return addResult
    }
}

class FakeNotebookRepository : NotebookRepository {
    var noteResult: Result<Note> = Result.success(TestData.note)
    var saveResult: Result<Note> = Result.success(TestData.note)
    var deleteResult: Result<Unit> = Result.success(Unit)
    val getCalls = mutableListOf<Int>()
    val saveCalls = mutableListOf<List<Any?>>()
    val deleteCalls = mutableListOf<Int>()

    override suspend fun getNote(investigationId: Int): Result<Note> {
        getCalls += investigationId
        return noteResult
    }

    override suspend fun saveNote(
        investigationId: Int,
        text: String,
    ): Result<Note> {
        saveCalls += listOf(investigationId, text)
        return saveResult
    }

    override suspend fun deleteNote(noteId: Int): Result<Unit> {
        deleteCalls += noteId
        return deleteResult
    }
}

class FakePhotoRepository : PhotoRepository {
    var saveResult: Result<Unit> = Result.success(Unit)
    var uploadResult: Result<Unit> = Result.success(Unit)
    val saveCalls = mutableListOf<List<Any?>>()
    var uploadCalls = 0

    override suspend fun savePhoto(
        clueId: Int,
        filePath: String,
    ): Result<Unit> {
        saveCalls += listOf(clueId, filePath)
        return saveResult
    }

    override suspend fun uploadPending(): Result<Unit> {
        uploadCalls++
        return uploadResult
    }
}

class FakeProgressRepository : ProgressRepository {
    var progressResult: Result<InvestigationProgress> = Result.success(TestData.progress)
    var syncResult: Result<Unit> = Result.success(Unit)
    val getCalls = mutableListOf<Int>()
    var syncCalls = 0

    override suspend fun getProgress(caseId: Int): Result<InvestigationProgress> {
        getCalls += caseId
        return progressResult
    }

    override suspend fun saveProgress(progress: InvestigationProgress): Result<Unit> = Result.success(Unit)

    override suspend fun completeCase(caseId: Int): Result<Unit> = Result.success(Unit)

    override suspend fun syncProgress(): Result<Unit> {
        syncCalls++
        return syncResult
    }
}

class FakePuzzleRepository : PuzzleRepository {
    var answerResult: Result<Boolean> = Result.success(true)
    val answerCalls = mutableListOf<List<Any?>>()

    override suspend fun getPuzzle(id: Int): Result<Puzzle> = Result.success(Puzzle(id, "...", "SOS", null))

    override suspend fun markSolved(id: Int): Result<Unit> = Result.success(Unit)

    override suspend fun checkAnswer(
        investigationId: Int,
        puzzleId: Int,
        answer: String,
    ): Result<Boolean> {
        answerCalls += listOf(investigationId, puzzleId, answer)
        return answerResult
    }
}

class FakeSceneRepository : SceneRepository {
    var sceneResult: Result<String> = Result.success("Описание сцены")
    val calls = mutableListOf<List<Any?>>()

    override suspend fun getScene(
        caseId: Int,
        sceneId: Int,
    ): Result<String> {
        calls += listOf(caseId, sceneId)
        return sceneResult
    }
}

class FakeSuspectRepository : SuspectRepository {
    var suspectsResult: Result<List<Suspect>> = Result.success(listOf(TestData.suspect))
    val calls = mutableListOf<Int>()

    override suspend fun getSuspects(caseId: Int): Result<List<Suspect>> {
        calls += caseId
        return suspectsResult
    }

    override suspend fun getSuspect(id: Int): Result<Suspect> = Result.success(TestData.suspect)
}

class FakeMorseSignalPlayer : MorseSignalPlayer {
    var available = true

    override fun isAvailable(): Boolean = available
    var error: Throwable? = null
    val played = mutableListOf<String>()

    override suspend fun play(pattern: String) {
        played += pattern
        error?.let { throw it }
    }
}
