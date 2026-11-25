package com.example.mrdetective.domain.usecases.auth

import com.example.mrdetective.domain.fakes.FakeAuthRepository
import com.example.mrdetective.domain.fakes.assertValidationError
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Проверяет [SignInUseCase] на заданных входных данных с тестовыми зависимостями. */
class SignInUseCaseTest {
    private lateinit var repository: FakeAuthRepository
    private lateinit var useCase: SignInUseCase

    /** Перед каждым тестом заново создаёт тестовый репозиторий и сценарий. */
    @Before
    fun setUp() {
        repository = FakeAuthRepository()
        useCase = SignInUseCase(repository)
    }

    /** Проверяет сценарий «успешный вход возвращает id пользователя». */
    @Test
    fun `успешный вход возвращает id пользователя`() =
        runTest {
            val result = useCase("a@b.ru", "123456")

            assertEquals("user-1", result.getOrNull())
            assertEquals(listOf<Any?>("a@b.ru", "123456"), repository.signInCalls.single())
        }

    /** Проверяет сценарий «пустой email возвращает ошибку валидации». */
    @Test
    fun `пустой email возвращает ошибку валидации`() =
        runTest {
            assertValidationError(useCase("  ", "123456"))
            assertTrue(repository.signInCalls.isEmpty())
        }

    /** Проверяет сценарий «некорректный адрес не отправляется на сервер». */
    @Test
    fun `некорректный адрес не отправляется на сервер`() =
        runTest {
            assertValidationError(useCase("user@localhost", "123456"))
            assertTrue(repository.signInCalls.isEmpty())
        }
}

/** Проверяет [SignUpUseCase] на заданных входных данных с тестовыми зависимостями. */
class SignUpUseCaseTest {
    private lateinit var repository: FakeAuthRepository
    private lateinit var useCase: SignUpUseCase

    /** Перед каждым тестом заново создаёт тестовый репозиторий и сценарий. */
    @Before
    fun setUp() {
        repository = FakeAuthRepository()
        useCase = SignUpUseCase(repository)
    }

    /** Проверяет сценарий «успешная регистрация возвращает id пользователя». */
    @Test
    fun `успешная регистрация возвращает id пользователя`() =
        runTest {
            val result = useCase("a@b.ru", "123456")

            assertEquals("user-1", result.getOrNull())
            assertEquals(listOf<Any?>("a@b.ru", "123456"), repository.signUpCalls.single())
        }

    /** Проверяет сценарий «пустые поля возвращают ошибку валидации». */
    @Test
    fun `пустые поля возвращают ошибку валидации`() =
        runTest {
            assertValidationError(useCase("", "123456"))
            assertValidationError(useCase("a@b.ru", " "))
            assertTrue(repository.signUpCalls.isEmpty())
        }

    /** Проверяет сценарий «короткий пароль не отправляется на сервер». */
    @Test
    fun `короткий пароль не отправляется на сервер`() =
        runTest {
            assertValidationError(useCase("a@b.ru", "12345"))
            assertTrue(repository.signUpCalls.isEmpty())
        }
}
