package com.example.mrdetective.domain.usecases.puzzles

import com.example.mrdetective.domain.fakes.FakePuzzleRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Unit tests for the behavior of [PlayCodeChallengeUseCase] using controlled inputs and dependencies. */
class PlayCodeChallengeUseCaseTest {
    private val repository = FakePuzzleRepository()
    private val playCode = PlayCodeChallengeUseCase(PlayPuzzleUseCase(repository))

    /** Sends four digits including the leading zero. */
    @Test
    fun sendsFourDigitsIncludingTheLeadingZero() =
        runTest {
            assertTrue(playCode(1, 2, "0417").getOrThrow())
            assertEquals(listOf(1, 2, "0417"), repository.answerCalls.single())
        }

    /** Invalid codes never reach the server. */
    @Test
    fun invalidCodesNeverReachTheServer() =
        runTest {
            for (answer in listOf("417", "04170", "04:7", "０４１７", " 0417")) {
                assertTrue(playCode(1, 2, answer).isFailure)
            }
            assertTrue(repository.answerCalls.isEmpty())
        }

    /** Keeps the server decision for awell formed wrong code. */
    @Test
    fun keepsTheServerDecisionForAWellFormedWrongCode() =
        runTest {
            repository.answerResult = Result.success(false)
            assertFalse(playCode(1, 2, "0000").getOrThrow())
        }
}
