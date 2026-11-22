package com.example.mrdetective.domain.usecases.puzzles

import com.example.mrdetective.domain.fakes.FakePuzzleRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayCodeChallengeUseCaseTest {
    private val repository = FakePuzzleRepository()
    private val playCode = PlayCodeChallengeUseCase(PlayPuzzleUseCase(repository))

    @Test
    fun sendsFourDigitsIncludingTheLeadingZero() =
        runTest {
            assertTrue(playCode(1, 2, "0417").getOrThrow())
            assertEquals(listOf(1, 2, "0417"), repository.answerCalls.single())
        }

    @Test
    fun invalidCodesNeverReachTheServer() =
        runTest {
            for (answer in listOf("417", "04170", "04:7", "０４１７", " 0417")) {
                assertTrue(playCode(1, 2, answer).isFailure)
            }
            assertTrue(repository.answerCalls.isEmpty())
        }

    @Test
    fun keepsTheServerDecisionForAWellFormedWrongCode() =
        runTest {
            repository.answerResult = Result.success(false)
            assertFalse(playCode(1, 2, "0000").getOrThrow())
        }
}
