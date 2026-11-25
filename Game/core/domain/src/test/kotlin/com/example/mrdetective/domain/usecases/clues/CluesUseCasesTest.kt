package com.example.mrdetective.domain.usecases.clues

import com.example.mrdetective.domain.fakes.FakeClueRepository
import com.example.mrdetective.domain.fakes.FakeInventoryRepository
import com.example.mrdetective.domain.fakes.FakePhotoRepository
import com.example.mrdetective.domain.fakes.TestData
import com.example.mrdetective.domain.fakes.assertValidationError
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Unit tests for the behavior of [AddToInventoryUseCase] using controlled inputs and dependencies. */
class AddToInventoryUseCaseTest {
    private val repository = FakeInventoryRepository()
    private val useCase = AddToInventoryUseCase(repository)

    /** Проверяет сценарий «добавляет улику в инвентарь расследования». */
    @Test
    fun `добавляет улику в инвентарь расследования`() =
        runTest {
            val result = useCase(TestData.clue, TestData.progress)

            assertEquals(TestData.collectedClue, result.getOrNull())
            assertEquals(listOf<Any?>(TestData.progress.id, TestData.clue.id), repository.addCalls.single())
        }

    /** Проверяет сценарий «улика с некорректным id возвращает ошибку валидации». */
    @Test
    fun `улика с некорректным id возвращает ошибку валидации`() =
        runTest {
            assertValidationError(useCase(TestData.clue.copy(id = 0), TestData.progress))
            assertTrue(repository.addCalls.isEmpty())
        }
}

/** Unit tests for the behavior of [CollectClueUseCase] using controlled inputs and dependencies. */
class CollectClueUseCaseTest {
    private val repository = FakeClueRepository()
    private val useCase = CollectClueUseCase(repository)

    /** Проверяет сценарий «собирает улику». */
    @Test
    fun `собирает улику`() =
        runTest {
            assertTrue(useCase(7).isSuccess)
            assertEquals(listOf(7), repository.collectCalls)
        }

    /** Проверяет сценарий «некорректный id улики возвращает ошибку валидации». */
    @Test
    fun `некорректный id улики возвращает ошибку валидации`() =
        runTest {
            assertValidationError(useCase(0))
            assertTrue(repository.collectCalls.isEmpty())
        }
}

/** Unit tests for the behavior of [ConfirmClueApproachUseCase] using controlled inputs and dependencies. */
class ConfirmClueApproachUseCaseTest {
    private val useCase = ConfirmClueApproachUseCase()

    /** Проверяет сценарий «расстояние меньше требуемого — подтверждено». */
    @Test
    fun `расстояние меньше требуемого — подтверждено`() {
        assertTrue(useCase(5f, 10f))
    }

    /** Проверяет сценарий «расстояние больше требуемого — не подтверждено». */
    @Test
    fun `расстояние больше требуемого — не подтверждено`() {
        assertFalse(useCase(10.1f, 10f))
    }
}

/** Unit tests for the behavior of [SaveCluePhotoUseCase] using controlled inputs and dependencies. */
class SaveCluePhotoUseCaseTest {
    private val repository = FakePhotoRepository()
    private val useCase = SaveCluePhotoUseCase(repository)

    /** Проверяет сценарий «сохраняет фото улики». */
    @Test
    fun `сохраняет фото улики`() =
        runTest {
            assertTrue(useCase(2, "/photos/a.jpg").isSuccess)
            assertEquals(listOf<Any?>(2, "/photos/a.jpg"), repository.saveCalls.single())
        }

    /** Проверяет сценарий «некорректные данные возвращают ошибку валидации». */
    @Test
    fun `некорректные данные возвращают ошибку валидации`() =
        runTest {
            assertValidationError(useCase(0, "/photos/a.jpg"))
            assertValidationError(useCase(1, " "))
            assertTrue(repository.saveCalls.isEmpty())
        }
}
