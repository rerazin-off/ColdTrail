package com.example.mrdetective.domain.usecases.cases

import com.example.mrdetective.domain.fakes.FakeCaseRepository
import com.example.mrdetective.domain.fakes.FakePhotoRepository
import com.example.mrdetective.domain.fakes.FakeProgressRepository
import com.example.mrdetective.domain.fakes.FakeSuspectRepository
import com.example.mrdetective.domain.fakes.TestData
import com.example.mrdetective.domain.fakes.assertFailureWith
import com.example.mrdetective.domain.fakes.assertValidationError
import com.example.mrdetective.domain.models.DomainError
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Проверяет [LoadAvailableCasesUseCase] на заданных входных данных с тестовыми зависимостями. */
class LoadAvailableCasesUseCaseTest {
    private val repository = FakeCaseRepository()
    private val useCase = LoadAvailableCasesUseCase(repository)

    /** Проверяет сценарий «возвращает список дел из репозитория». */
    @Test
    fun `возвращает список дел из репозитория`() =
        runTest {
            val result = useCase()

            assertEquals(listOf(TestData.case), result.getOrNull())
            assertEquals(1, repository.availableCalls)
        }

    /** Проверяет сценарий «ошибка репозитория возвращается в результате». */
    @Test
    fun `ошибка репозитория возвращается в результате`() =
        runTest {
            val error = DomainError.Network()
            repository.availableResult = Result.failure(error)

            assertFailureWith(error, useCase())
        }
}

/** Проверяет [LoadSuspectsUseCase] на заданных входных данных с тестовыми зависимостями. */
class LoadSuspectsUseCaseTest {
    private val repository = FakeSuspectRepository()
    private val useCase = LoadSuspectsUseCase(repository)

    /** Проверяет сценарий «возвращает подозреваемых дела». */
    @Test
    fun `возвращает подозреваемых дела`() =
        runTest {
            val result = useCase(5)

            assertEquals(listOf(TestData.suspect), result.getOrNull())
            assertEquals(listOf(5), repository.calls)
        }

    /** Проверяет сценарий «некорректный id дела возвращает ошибку валидации». */
    @Test
    fun `некорректный id дела возвращает ошибку валидации`() =
        runTest {
            assertValidationError(useCase(0))
            assertValidationError(useCase(-1))
            assertTrue(repository.calls.isEmpty())
        }
}

/**
 * Проверяет [RestoreInvestigationProgressUseCase] на заданных входных данных с тестовыми зависимостями.
 */
class RestoreInvestigationProgressUseCaseTest {
    private val repository = FakeProgressRepository()
    private val useCase = RestoreInvestigationProgressUseCase(repository)

    /** Проверяет сценарий «возвращает прогресс расследования». */
    @Test
    fun `возвращает прогресс расследования`() =
        runTest {
            val result = useCase(1)

            assertEquals(TestData.progress, result.getOrNull())
            assertEquals(listOf(1), repository.getCalls)
        }

    /** Проверяет сценарий «некорректный id дела возвращает ошибку валидации». */
    @Test
    fun `некорректный id дела возвращает ошибку валидации`() =
        runTest {
            assertValidationError(useCase(0))
            assertTrue(repository.getCalls.isEmpty())
        }
}

/** Проверяет [StartInvestigationUseCase] на заданных входных данных с тестовыми зависимостями. */
class StartInvestigationUseCaseTest {
    private val repository = FakeCaseRepository()
    private val useCase = StartInvestigationUseCase(repository)

    /** Проверяет сценарий «запускает дело в репозитории». */
    @Test
    fun `запускает дело в репозитории`() =
        runTest {
            val result = useCase(3)

            assertTrue(result.isSuccess)
            assertEquals(listOf(3), repository.startCalls)
        }

    /** Проверяет сценарий «некорректный id дела возвращает ошибку валидации». */
    @Test
    fun `некорректный id дела возвращает ошибку валидации`() =
        runTest {
            assertValidationError(useCase(0))
            assertTrue(repository.startCalls.isEmpty())
        }
}

/** Проверяет [SyncProgressUseCase] на заданных входных данных с тестовыми зависимостями. */
class SyncProgressUseCaseTest {
    private val progress = FakeProgressRepository()
    private val photos = FakePhotoRepository()
    private val useCase = SyncProgressUseCase(progress, photos)

    /** Проверяет сценарий «синхронизирует прогресс и загружает фото». */
    @Test
    fun `синхронизирует прогресс и загружает фото`() =
        runTest {
            val result = useCase()

            assertTrue(result.isSuccess)
            assertEquals(1, progress.syncCalls)
            assertEquals(1, photos.uploadCalls)
        }

    /** Проверяет сценарий «при ошибке синхронизации прогресса фото не загружаются». */
    @Test
    fun `при ошибке синхронизации прогресса фото не загружаются`() =
        runTest {
            val error = DomainError.Network()
            progress.syncResult = Result.failure(error)

            assertFailureWith(error, useCase())
            assertEquals(0, photos.uploadCalls)
        }
}
