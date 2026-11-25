package com.example.mrdetective.domain.usecases.notebook

import com.example.mrdetective.domain.fakes.FakeNotebookRepository
import com.example.mrdetective.domain.fakes.TestData
import com.example.mrdetective.domain.fakes.assertValidationError
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Unit tests for the behavior of [LoadNotebookUseCase] using controlled inputs and dependencies. */
class LoadNotebookUseCaseTest {
    private lateinit var repository: FakeNotebookRepository
    private lateinit var useCase: LoadNotebookUseCase

    /** Recreates the fake repository and use case before each test. */
    @Before
    fun setUp() {
        repository = FakeNotebookRepository()
        useCase = LoadNotebookUseCase(repository)
    }

    /** Проверяет сценарий «загрузка записки возвращает заметку». */
    @Test
    fun `загрузка записки возвращает заметку`() =
        runTest {
            val result = useCase(1)

            assertTrue(result.isSuccess)
            assertEquals(TestData.note, result.getOrNull())
            assertEquals(listOf(1), repository.getCalls)
        }

    /** Проверяет сценарий «некорректный ID возвращает ошибку». */
    @Test
    fun `некорректный ID возвращает ошибку`() =
        runTest {
            assertValidationError(useCase(0))
            assertTrue(repository.getCalls.isEmpty())
        }
}

/** Unit tests for the behavior of [SaveNoteUseCase] using controlled inputs and dependencies. */
class SaveNoteUseCaseTest {
    private val repository = FakeNotebookRepository()
    private val useCase = SaveNoteUseCase(repository)

    /** Проверяет сценарий «сохраняет заметку». */
    @Test
    fun `сохраняет заметку`() =
        runTest {
            val result = useCase(10, "Текст")

            assertEquals(TestData.note, result.getOrNull())
            assertEquals(listOf<Any?>(10, "Текст"), repository.saveCalls.single())
        }

    /** Проверяет сценарий «некорректные данные возвращают ошибку валидации». */
    @Test
    fun `некорректные данные возвращают ошибку валидации`() =
        runTest {
            assertValidationError(useCase(0, "Текст"))
            assertValidationError(useCase(10, "  "))
            assertTrue(repository.saveCalls.isEmpty())
        }
}

/** Unit tests for the behavior of [DeleteNoteUseCase] using controlled inputs and dependencies. */
class DeleteNoteUseCaseTest {
    private val repository = FakeNotebookRepository()
    private val useCase = DeleteNoteUseCase(repository)

    /** Проверяет сценарий «удаляет заметку». */
    @Test
    fun `удаляет заметку`() =
        runTest {
            assertTrue(useCase(3).isSuccess)
            assertEquals(listOf(3), repository.deleteCalls)
        }

    /** Проверяет сценарий «некорректный id возвращает ошибку валидации». */
    @Test
    fun `некорректный id возвращает ошибку валидации`() =
        runTest {
            assertValidationError(useCase(0))
            assertTrue(repository.deleteCalls.isEmpty())
        }
}
