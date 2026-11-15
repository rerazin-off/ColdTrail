package com.example.mrdetective.domain.usecases.notebook

import com.example.mrdetective.domain.fakes.FakeNotebookRepository
import com.example.mrdetective.domain.fakes.TestData
import com.example.mrdetective.domain.fakes.assertValidationError
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LoadNotebookUseCaseTest {
    private lateinit var repository: FakeNotebookRepository
    private lateinit var useCase: LoadNotebookUseCase

    @Before
    fun setUp() {
        repository = FakeNotebookRepository()
        useCase = LoadNotebookUseCase(repository)
    }

    @Test
    fun `загрузка записки возвращает заметку`() =
        runTest {
            val result = useCase(1)

            assertTrue(result.isSuccess)
            assertEquals(TestData.note, result.getOrNull())
            assertEquals(listOf(1), repository.getCalls)
        }

    @Test
    fun `некорректный ID возвращает ошибку`() =
        runTest {
            assertValidationError(useCase(0))
            assertTrue(repository.getCalls.isEmpty())
        }
}

class SaveNoteUseCaseTest {
    private val repository = FakeNotebookRepository()
    private val useCase = SaveNoteUseCase(repository)

    @Test
    fun `сохраняет заметку`() =
        runTest {
            val result = useCase(10, "Текст")

            assertEquals(TestData.note, result.getOrNull())
            assertEquals(listOf<Any?>(10, "Текст"), repository.saveCalls.single())
        }

    @Test
    fun `некорректные данные возвращают ошибку валидации`() =
        runTest {
            assertValidationError(useCase(0, "Текст"))
            assertValidationError(useCase(10, "  "))
            assertTrue(repository.saveCalls.isEmpty())
        }
}

class DeleteNoteUseCaseTest {
    private val repository = FakeNotebookRepository()
    private val useCase = DeleteNoteUseCase(repository)

    @Test
    fun `удаляет заметку`() =
        runTest {
            assertTrue(useCase(3).isSuccess)
            assertEquals(listOf(3), repository.deleteCalls)
        }

    @Test
    fun `некорректный id возвращает ошибку валидации`() =
        runTest {
            assertValidationError(useCase(0))
            assertTrue(repository.deleteCalls.isEmpty())
        }
}
