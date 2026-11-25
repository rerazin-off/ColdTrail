package com.example.mrdetective.domain.fakes

import com.example.mrdetective.domain.models.DomainError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

/** Asserts that the result failed with a domain validation error. */
fun assertValidationError(result: Result<*>) {
    assertTrue("Ожидалась ошибка, а пришло $result", result.isFailure)
    val error = result.exceptionOrNull()
    assertTrue("Ожидался DomainError.Validation, а пришло $error", error is DomainError.Validation)
}

/** Asserts that the result failed with the expected type of error. */
fun assertFailureWith(
    expected: Throwable,
    result: Result<*>,
) {
    assertTrue(result.isFailure)
    assertEquals(expected, result.exceptionOrNull())
}
