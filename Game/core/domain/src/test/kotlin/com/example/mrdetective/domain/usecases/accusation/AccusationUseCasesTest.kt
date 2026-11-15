package com.example.mrdetective.domain.usecases.accusation

import com.example.mrdetective.domain.fakes.*
import com.example.mrdetective.domain.usecases.accusation.SubmitAccusationUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SubmitAccusationUseCaseTest {
    private val repository = FakeAccusationRepository()
    private val useCase = SubmitAccusationUseCase(repository)

    @Test
    fun `отправляет обвинение`() = runTest {
        val result = useCase(1, 2, listOf(3, 4))

        assertEquals(TestData.accusation, result.getOrNull())
        assertEquals(listOf<Any?>(1, 2, listOf(3, 4)), repository.calls.single())
    }

    @Test
    fun `некорректные данные возвращают ошибку валидации`() = runTest {
        assertValidationError(useCase(0, 2, emptyList()))
        assertValidationError(useCase(1, 0, emptyList()))
        assertTrue(repository.calls.isEmpty())
    }
}
