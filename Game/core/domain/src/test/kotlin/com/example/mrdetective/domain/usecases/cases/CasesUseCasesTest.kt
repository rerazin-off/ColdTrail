package com.example.mrdetective.domain.usecases.cases

import com.example.mrdetective.domain.fakes.*
import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.usecases.cases.LoadAvailableCasesUseCase
import com.example.mrdetective.domain.usecases.cases.LoadSuspectsUseCase
import com.example.mrdetective.domain.usecases.cases.RestoreInvestigationProgressUseCase
import com.example.mrdetective.domain.usecases.cases.StartInvestigationUseCase
import com.example.mrdetective.domain.usecases.cases.SyncProgressUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LoadAvailableCasesUseCaseTest {
    private val repository = FakeCaseRepository()
    private val useCase = LoadAvailableCasesUseCase(repository)

    @Test
    fun `возвращает список дел из репозитория`() = runTest {
        val result = useCase()

        assertEquals(listOf(TestData.case), result.getOrNull())
        assertEquals(1, repository.availableCalls)
    }

    @Test
    fun `ошибка репозитория возвращается в результате`() = runTest {
        val error = DomainError.Network()
        repository.availableResult = Result.failure(error)

        assertFailureWith(error, useCase())
    }
}

class LoadSuspectsUseCaseTest {
    private val repository = FakeSuspectRepository()
    private val useCase = LoadSuspectsUseCase(repository)

    @Test
    fun `возвращает подозреваемых дела`() = runTest {
        val result = useCase(5)

        assertEquals(listOf(TestData.suspect), result.getOrNull())
        assertEquals(listOf(5), repository.calls)
    }

    @Test
    fun `некорректный id дела возвращает ошибку валидации`() = runTest {
        assertValidationError(useCase(0))
        assertValidationError(useCase(-1))
        assertTrue(repository.calls.isEmpty())
    }
}

class RestoreInvestigationProgressUseCaseTest {
    private val repository = FakeProgressRepository()
    private val useCase = RestoreInvestigationProgressUseCase(repository)

    @Test
    fun `возвращает прогресс расследования`() = runTest {
        val result = useCase(1)

        assertEquals(TestData.progress, result.getOrNull())
        assertEquals(listOf(1), repository.getCalls)
    }

    @Test
    fun `некорректный id дела возвращает ошибку валидации`() = runTest {
        assertValidationError(useCase(0))
        assertTrue(repository.getCalls.isEmpty())
    }
}

class StartInvestigationUseCaseTest {
    private val repository = FakeCaseRepository()
    private val useCase = StartInvestigationUseCase(repository)

    @Test
    fun `запускает дело в репозитории`() = runTest {
        val result = useCase(3)

        assertTrue(result.isSuccess)
        assertEquals(listOf(3), repository.startCalls)
    }

    @Test
    fun `некорректный id дела возвращает ошибку валидации`() = runTest {
        assertValidationError(useCase(0))
        assertTrue(repository.startCalls.isEmpty())
    }
}

class SyncProgressUseCaseTest {
    private val progress = FakeProgressRepository()
    private val photos = FakePhotoRepository()
    private val useCase = SyncProgressUseCase(progress, photos)

    @Test
    fun `синхронизирует прогресс и загружает фото`() = runTest {
        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(1, progress.syncCalls)
        assertEquals(1, photos.uploadCalls)
    }

    @Test
    fun `при ошибке синхронизации прогресса фото не загружаются`() = runTest {
        val error = DomainError.Network()
        progress.syncResult = Result.failure(error)

        assertFailureWith(error, useCase())
        assertEquals(0, photos.uploadCalls)
    }
}
