package com.example.mrdetective.domain.usecases.auth

import com.example.mrdetective.domain.fakes.*
import com.example.mrdetective.domain.usecases.auth.SignInUseCase
import com.example.mrdetective.domain.usecases.auth.SignUpUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SignInUseCaseTest {
    private lateinit var repository: FakeAuthRepository
    private lateinit var useCase: SignInUseCase

    @Before
    fun setUp() {
        repository = FakeAuthRepository()
        useCase = SignInUseCase(repository)
    }

    @Test
    fun `успешный вход возвращает id пользователя`() = runTest {
        val result = useCase("a@b.ru", "123456")

        assertEquals("user-1", result.getOrNull())
        assertEquals(listOf<Any?>("a@b.ru", "123456"), repository.signInCalls.single())
    }

    @Test
    fun `пустой email возвращает ошибку валидации`() = runTest {
        assertValidationError(useCase("  ", "123456"))
        assertTrue(repository.signInCalls.isEmpty())
    }
}

class SignUpUseCaseTest {
    private lateinit var repository: FakeAuthRepository
    private lateinit var useCase: SignUpUseCase

    @Before
    fun setUp() {
        repository = FakeAuthRepository()
        useCase = SignUpUseCase(repository)
    }

    @Test
    fun `успешная регистрация возвращает id пользователя`() = runTest {
        val result = useCase("a@b.ru", "123456")

        assertEquals("user-1", result.getOrNull())
        assertEquals(listOf<Any?>("a@b.ru", "123456"), repository.signUpCalls.single())
    }

    @Test
    fun `пустые поля возвращают ошибку валидации`() = runTest {
        assertValidationError(useCase("", "123456"))
        assertValidationError(useCase("a@b.ru", " "))
        assertTrue(repository.signUpCalls.isEmpty())
    }
}
