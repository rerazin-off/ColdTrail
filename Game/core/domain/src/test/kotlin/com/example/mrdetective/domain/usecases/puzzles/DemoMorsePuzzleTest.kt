package com.example.mrdetective.domain.usecases.puzzles

import org.junit.Assert.assertEquals
import org.junit.Test

class DemoMorsePuzzleTest {
    @Test
    fun `проверяет только полный цифровой код`() {
        assertEquals(MorseCodeCheck.Incomplete, DemoMorsePuzzle.check(""))
        assertEquals(MorseCodeCheck.Incomplete, DemoMorsePuzzle.check("221"))
        assertEquals(MorseCodeCheck.Incomplete, DemoMorsePuzzle.check("22а4"))
        assertEquals(MorseCodeCheck.Incomplete, DemoMorsePuzzle.check("22144"))
    }

    @Test
    fun `различает верный и неверный коды`() {
        assertEquals(MorseCodeCheck.Correct, DemoMorsePuzzle.check("2214"))
        assertEquals(MorseCodeCheck.Incorrect, DemoMorsePuzzle.check("2219"))
    }
}
