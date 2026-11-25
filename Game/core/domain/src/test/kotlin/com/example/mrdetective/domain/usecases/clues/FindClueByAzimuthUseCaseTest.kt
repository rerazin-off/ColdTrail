package com.example.mrdetective.domain.usecases.clues

import com.example.mrdetective.domain.fakes.TestData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Проверяет поиск ближайшей улики по круговой шкале азимута. */
class FindClueByAzimuthUseCaseTest {
    private val findClue = FindClueByAzimuthUseCase()

    /** Находит ближайшую улику при переходе через север. */
    @Test
    fun findsNearestAcrossNorth() {
        val west = TestData.clue.copy(id = 1)
        val east = TestData.clue.copy(id = 2)
        assertEquals(east, findClue(listOf(west, east), 359f, mapOf(west.id to 350f, east.id to 1f)))
    }

    /** Пропускает улики с неизвестным азимутом. */
    @Test
    fun skipsCluesWithoutBearing() {
        assertNull(findClue(listOf(TestData.clue), 0f, emptyMap()))
    }
}
