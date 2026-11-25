package com.example.mrdetective.domain.usecases.puzzles

import com.example.mrdetective.domain.models.MorseChallenge
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Tests the eligibility rules and local input validation for Morse puzzles. */
class MorseChallengeRulesTest {
    private val puzzle = MorseChallenge(1, "Морзе", null, ".- .-. -", 10, solved = false, rewardClaimed = false)

    /** New game skips solved puzzles and rewards already collected. */
    @Test
    fun `new game skips solved puzzles and rewards already collected`() {
        val solved = puzzle.copy(id = 2, solved = true)
        val claimed = puzzle.copy(id = 3, rewardClaimed = true)
        assertEquals(puzzle, chooseMorseChallenge(listOf(solved, puzzle, claimed), restoredId = null))
        assertNull(chooseMorseChallenge(listOf(solved, claimed), restoredId = null))
        assertEquals(solved, chooseMorseChallenge(listOf(solved, puzzle), restoredId = solved.id))
    }

    /** Local validation checks shape but leaves correctness to the server. */
    @Test
    fun `local validation checks shape but leaves correctness to the server`() {
        assertTrue(validateMorseChallengeAnswer(puzzle, "ART"))
        assertTrue(validateMorseChallengeAnswer(puzzle, "ABC"))
        assertFalse(validateMorseChallengeAnswer(puzzle, "AR"))
        assertFalse(validateMorseChallengeAnswer(puzzle, "АRT"))
        val digits = puzzle.copy(signal = "..--- .----")
        assertTrue(validateMorseChallengeAnswer(digits, "21"))
        assertFalse(validateMorseChallengeAnswer(digits, "AB"))
    }
}
