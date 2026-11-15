package com.example.mrdetective.domain.usecases.dialogue

import com.example.mrdetective.domain.fakes.*
import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.usecases.dialogue.LoadDialogueHistoryUseCase
import com.example.mrdetective.domain.usecases.dialogue.PresentClueToSuspectUseCase
import com.example.mrdetective.domain.usecases.dialogue.SendSuspectMessageUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LoadDialogueHistoryUseCaseTest {
    private val repository = FakeDialogueRepository()
    private val useCase = LoadDialogueHistoryUseCase(repository)

    @Test
    fun `возвращает историю диалога`() = runTest {
        val result = useCase(1, 2)

        assertEquals(listOf(TestData.message), result.getOrNull())
        assertEquals(listOf<Any?>(1, 2), repository.historyCalls.single())
    }

    @Test
    fun `ошибка репозитория возвращается в результате`() = runTest {
        val error = DomainError.Network()
        repository.historyResult = Result.failure(error)

        assertFailureWith(error, useCase(1, 2))
    }
}

class PresentClueToSuspectUseCaseTest {
    private val repository = FakeDialogueRepository()
    private val useCase = PresentClueToSuspectUseCase(repository)

    @Test
    fun `предъявляет улику подозреваемому`() = runTest {
        val result = useCase(1, 2, 3)

        assertEquals(TestData.message, result.getOrNull())
        assertEquals(listOf<Any?>(1, 2, 3), repository.presentCalls.single())
    }

    @Test
    fun `некорректные идентификаторы возвращают ошибку валидации`() = runTest {
        assertValidationError(useCase(0, 2, 3))
        assertValidationError(useCase(1, 0, 3))
        assertValidationError(useCase(1, 2, 0))
        assertTrue(repository.presentCalls.isEmpty())
    }
}

class SendSuspectMessageUseCaseTest {
    private val repository = FakeDialogueRepository()
    private val useCase = SendSuspectMessageUseCase(repository)

    @Test
    fun `отправляет сообщение подозреваемому`() = runTest {
        val result = useCase(1, 2, "Где вы были?")

        assertEquals(TestData.message, result.getOrNull())
        assertEquals(listOf<Any?>(1, 2, "Где вы были?"), repository.sendCalls.single())
    }

    @Test
    fun `пустое сообщение возвращает ошибку валидации`() = runTest {
        assertValidationError(useCase(1, 2, "   "))
        assertTrue(repository.sendCalls.isEmpty())
    }
}
