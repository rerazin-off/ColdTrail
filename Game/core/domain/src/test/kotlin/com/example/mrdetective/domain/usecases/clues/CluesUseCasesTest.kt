package com.example.mrdetective.domain.usecases.clues

import com.example.mrdetective.domain.fakes.*
import com.example.mrdetective.domain.usecases.clues.AddToInventoryUseCase
import com.example.mrdetective.domain.usecases.clues.CollectClueUseCase
import com.example.mrdetective.domain.usecases.clues.ConfirmClueApproachUseCase
import com.example.mrdetective.domain.usecases.clues.FindClueByAzimuthUseCase
import com.example.mrdetective.domain.usecases.clues.SaveCluePhotoUseCase
import org.junit.Assert.assertFalse
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AddToInventoryUseCaseTest {
    private val repository = FakeInventoryRepository()
    private val useCase = AddToInventoryUseCase(repository)

    @Test
    fun `добавляет улику в инвентарь расследования`() = runTest {
        val result = useCase(TestData.clue, TestData.progress)

        assertEquals(TestData.collectedClue, result.getOrNull())
        assertEquals(listOf<Any?>(TestData.progress.id, TestData.clue.id), repository.addCalls.single())
    }

    @Test
    fun `улика с некорректным id возвращает ошибку валидации`() = runTest {
        assertValidationError(useCase(TestData.clue.copy(id = 0), TestData.progress))
        assertTrue(repository.addCalls.isEmpty())
    }
}

class CollectClueUseCaseTest {
    private val repository = FakeClueRepository()
    private val useCase = CollectClueUseCase(repository)

    @Test
    fun `собирает улику`() = runTest {
        assertTrue(useCase(7).isSuccess)
        assertEquals(listOf(7), repository.collectCalls)
    }

    @Test
    fun `некорректный id улики возвращает ошибку валидации`() = runTest {
        assertValidationError(useCase(0))
        assertTrue(repository.collectCalls.isEmpty())
    }
}

class ConfirmClueApproachUseCaseTest {
    private val useCase = ConfirmClueApproachUseCase()

    @Test
    fun `расстояние меньше требуемого — подтверждено`() {
        assertTrue(useCase(5f, 10f))
    }

    @Test
    fun `расстояние больше требуемого — не подтверждено`() {
        assertFalse(useCase(10.1f, 10f))
    }
}



class SaveCluePhotoUseCaseTest {
    private val repository = FakePhotoRepository()
    private val useCase = SaveCluePhotoUseCase(repository)

    @Test
    fun `сохраняет фото улики`() = runTest {
        assertTrue(useCase(2, "/photos/a.jpg").isSuccess)
        assertEquals(listOf<Any?>(2, "/photos/a.jpg"), repository.saveCalls.single())
    }

    @Test
    fun `некорректные данные возвращают ошибку валидации`() = runTest {
        assertValidationError(useCase(0, "/photos/a.jpg"))
        assertValidationError(useCase(1, " "))
        assertTrue(repository.saveCalls.isEmpty())
    }
}
