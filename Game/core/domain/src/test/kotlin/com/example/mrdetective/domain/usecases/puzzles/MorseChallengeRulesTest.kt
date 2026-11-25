package com.example.mrdetective.domain.usecases.puzzles

import com.example.mrdetective.domain.models.MorseChallenge
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Проверяет отбор головоломок Морзе и локальную проверку ввода. */
class MorseChallengeRulesTest {
    private val puzzle = MorseChallenge(1, "Морзе", null, ".- .-. -", 10, solved = false, rewardClaimed = false)

    /** Проверяет, что при выборе пропускаются решённые головоломки и уже полученные награды. */
    @Test
    fun `new game skips solved puzzles and rewards already collected`() {
        val solved = puzzle.copy(id = 2, solved = true)
        val claimed = puzzle.copy(id = 3, rewardClaimed = true)
        assertEquals(puzzle, chooseMorseChallenge(listOf(solved, puzzle, claimed), restoredId = null))
        assertNull(chooseMorseChallenge(listOf(solved, claimed), restoredId = null))
        assertEquals(solved, chooseMorseChallenge(listOf(solved, puzzle), restoredId = solved.id))
    }

    /** Проверяет, что клиент валидирует формат, а правильность ответа определяет сервер. */
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
