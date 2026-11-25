package com.example.mrdetective.domain.usecases.inventory

import com.example.mrdetective.domain.fakes.FakeClueRepository
import com.example.mrdetective.domain.fakes.FakeInventoryRepository
import com.example.mrdetective.domain.fakes.TestData
import com.example.mrdetective.domain.fakes.assertValidationError
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Unit tests for the behavior of [InspectItemUseCase] using controlled inputs and dependencies. */
class InspectItemUseCaseTest {
    private val clues = FakeClueRepository()
    private val inventory = FakeInventoryRepository()
    private val useCase = InspectItemUseCase(clues, inventory)

    /** Проверяет сценарий «возвращает улику по id предмета». */
    @Test
    fun `возвращает улику по id предмета`() =
        runTest {
            val result = useCase(1)

            assertEquals(TestData.clue, result.getOrNull())
            assertEquals(listOf(1), clues.getClueCalls)
        }

    /** Проверяет сценарий «некорректный id возвращает ошибку валидации». */
    @Test
    fun `некорректный id возвращает ошибку валидации`() =
        runTest {
            assertValidationError(useCase(0))
            assertTrue(clues.getClueCalls.isEmpty())
        }
}

/** Unit tests for the behavior of [LoadInventoryUseCase] using controlled inputs and dependencies. */
class LoadInventoryUseCaseTest {
    private val repository = FakeInventoryRepository()
    private val useCase = LoadInventoryUseCase(repository)

    /** Проверяет сценарий «возвращает инвентарь». */
    @Test
    fun `возвращает инвентарь`() =
        runTest {
            val result = useCase(4)

            assertEquals(listOf(TestData.clue), result.getOrNull())
            assertEquals(listOf(4), repository.inventoryCalls)
        }

    /** Проверяет сценарий «некорректный id возвращает ошибку валидации». */
    @Test
    fun `некорректный id возвращает ошибку валидации`() =
        runTest {
            assertValidationError(useCase(0))
            assertTrue(repository.inventoryCalls.isEmpty())
        }
}
