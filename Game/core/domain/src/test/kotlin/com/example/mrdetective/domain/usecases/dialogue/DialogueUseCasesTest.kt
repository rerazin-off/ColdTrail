package com.example.mrdetective.domain.usecases.dialogue

import com.example.mrdetective.domain.fakes.FakeDialogueRepository
import com.example.mrdetective.domain.fakes.TestData
import com.example.mrdetective.domain.fakes.assertFailureWith
import com.example.mrdetective.domain.fakes.assertValidationError
import com.example.mrdetective.domain.models.DomainError
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Unit tests for the behavior of [LoadDialogueHistoryUseCase] using controlled inputs and dependencies. */
class LoadDialogueHistoryUseCaseTest {
    private val repository = FakeDialogueRepository()
    private val useCase = LoadDialogueHistoryUseCase(repository)

    /** Проверяет сценарий «возвращает историю диалога». */
    @Test
    fun `возвращает историю диалога`() =
        runTest {
            val result = useCase(1, 2)

            assertEquals(listOf(TestData.message), result.getOrNull())
            assertEquals(listOf<Any?>(1, 2), repository.historyCalls.single())
        }

    /** Проверяет сценарий «ошибка репозитория возвращается в результате». */
    @Test
    fun `ошибка репозитория возвращается в результате`() =
        runTest {
            val error = DomainError.Network()
            repository.historyResult = Result.failure(error)

            assertFailureWith(error, useCase(1, 2))
        }
}

/** Unit tests for the behavior of [PresentClueToSuspectUseCase] using controlled inputs and dependencies. */
class PresentClueToSuspectUseCaseTest {
    private val repository = FakeDialogueRepository()
    private val useCase = PresentClueToSuspectUseCase(repository)

    /** Проверяет сценарий «предъявляет улику подозреваемому». */
    @Test
    fun `предъявляет улику подозреваемому`() =
        runTest {
            val result = useCase(1, 2, 3)

            assertEquals(TestData.message, result.getOrNull())
            assertEquals(listOf<Any?>(1, 2, 3), repository.presentCalls.single())
        }

    /** Проверяет сценарий «некорректные идентификаторы возвращают ошибку валидации». */
    @Test
    fun `некорректные идентификаторы возвращают ошибку валидации`() =
        runTest {
            assertValidationError(useCase(0, 2, 3))
            assertValidationError(useCase(1, 0, 3))
            assertValidationError(useCase(1, 2, 0))
            assertTrue(repository.presentCalls.isEmpty())
        }
}

/** Unit tests for the behavior of [SendSuspectMessageUseCase] using controlled inputs and dependencies. */
class SendSuspectMessageUseCaseTest {
    private val repository = FakeDialogueRepository()
    private val useCase = SendSuspectMessageUseCase(repository)

    /** Проверяет сценарий «отправляет сообщение подозреваемому». */
    @Test
    fun `отправляет сообщение подозреваемому`() =
        runTest {
            val result = useCase(1, 2, "Где вы были?")

            assertEquals(TestData.message, result.getOrNull())
            assertEquals(listOf<Any?>(1, 2, "Где вы были?"), repository.sendCalls.single())
        }

    /** Проверяет сценарий «пустое сообщение возвращает ошибку валидации». */
    @Test
    fun `пустое сообщение возвращает ошибку валидации`() =
        runTest {
            assertValidationError(useCase(1, 2, "   "))
            assertTrue(repository.sendCalls.isEmpty())
        }
}
