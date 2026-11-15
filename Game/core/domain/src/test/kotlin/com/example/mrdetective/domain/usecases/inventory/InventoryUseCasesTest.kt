package com.example.mrdetective.domain.usecases.inventory

import com.example.mrdetective.domain.fakes.*
import com.example.mrdetective.domain.usecases.inventory.InspectItemUseCase
import com.example.mrdetective.domain.usecases.inventory.LoadInventoryUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InspectItemUseCaseTest {
    private val clues = FakeClueRepository()
    private val inventory = FakeInventoryRepository()
    private val useCase = InspectItemUseCase(clues, inventory)

    @Test
    fun `возвращает улику по id предмета`() = runTest {
        val result = useCase(1)

        assertEquals(TestData.clue, result.getOrNull())
        assertEquals(listOf(1), clues.getClueCalls)
    }

    @Test
    fun `некорректный id возвращает ошибку валидации`() = runTest {
        assertValidationError(useCase(0))
        assertTrue(clues.getClueCalls.isEmpty())
    }
}

class LoadInventoryUseCaseTest {
    private val repository = FakeInventoryRepository()
    private val useCase = LoadInventoryUseCase(repository)

    @Test
    fun `возвращает инвентарь`() = runTest {
        val result = useCase(4)

        assertEquals(listOf(TestData.clue), result.getOrNull())
        assertEquals(listOf(4), repository.inventoryCalls)
    }

    @Test
    fun `некорректный id возвращает ошибку валидации`() = runTest {
        assertValidationError(useCase(0))
        assertTrue(repository.inventoryCalls.isEmpty())
    }
}
