package com.example.mrdetective.domain.fakes

import com.example.mrdetective.domain.models.DomainError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

/** Проверяет, что операция завершилась ошибкой валидации домена. */
fun assertValidationError(result: Result<*>) {
    assertTrue("Ожидалась ошибка, а пришло $result", result.isFailure)
    val error = result.exceptionOrNull()
    assertTrue("Ожидался DomainError.Validation, а пришло $error", error is DomainError.Validation)
}

/** Проверяет, что операция завершилась ошибкой ожидаемого типа. */
fun assertFailureWith(
    expected: Throwable,
    result: Result<*>,
) {
    assertTrue(result.isFailure)
    assertEquals(expected, result.exceptionOrNull())
}
