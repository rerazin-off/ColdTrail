package com.example.mrdetective.domain.usecases.puzzles

import com.example.mrdetective.domain.fakes.FakeMorseSignalPlayer
import com.example.mrdetective.domain.fakes.FakePuzzleRepository
import com.example.mrdetective.domain.fakes.FakeSceneRepository
import com.example.mrdetective.domain.fakes.assertValidationError
import com.example.mrdetective.domain.models.DomainError
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Проверяет [InspectSceneUseCase] на заданных входных данных с тестовыми зависимостями. */
class InspectSceneUseCaseTest {
    private val repository = FakeSceneRepository()
    private val useCase = InspectSceneUseCase(repository)

    /** Проверяет сценарий «возвращает описание сцены». */
    @Test
    fun `возвращает описание сцены`() =
        runTest {
            val result = useCase(1, 2)

            assertEquals("Описание сцены", result.getOrNull())
            assertEquals(listOf<Any?>(1, 2), repository.calls.single())
        }

    /** Проверяет сценарий «некорректные идентификаторы возвращают ошибку валидации». */
    @Test
    fun `некорректные идентификаторы возвращают ошибку валидации`() =
        runTest {
            assertValidationError(useCase(0, 2))
            assertValidationError(useCase(1, 0))
            assertTrue(repository.calls.isEmpty())
        }
}

/** Проверяет [MatchFingerprintUseCase] на заданных входных данных с тестовыми зависимостями. */
class MatchFingerprintUseCaseTest {
    private val useCase = MatchFingerprintUseCase()

    /** Проверяет сценарий «одинаковые отпечатки совпадают». */
    @Test
    fun `одинаковые отпечатки совпадают`() {
        assertTrue(useCase("A1", "A1"))
    }

    /** Проверяет сценарий «разные отпечатки не совпадают». */
    @Test
    fun `разные отпечатки не совпадают`() {
        assertFalse(useCase("A1", "B2"))
    }
}

/** Проверяет [PlayMorseSignalUseCase] на заданных входных данных с тестовыми зависимостями. */
class PlayMorseSignalUseCaseTest {
    private val player = FakeMorseSignalPlayer()
    private val useCase = PlayMorseSignalUseCase(player)

    /** Проверяет сценарий «проигрывает сигнал». */
    @Test
    fun `проигрывает сигнал`() =
        runTest {
            assertTrue(useCase("... --- ...").isSuccess)
            assertEquals(listOf("... --- ..."), player.played)
        }

    /** Проверяет сценарий «пустой сигнал возвращает ошибку валидации». */
    @Test
    fun `пустой сигнал возвращает ошибку валидации`() =
        runTest {
            assertValidationError(useCase(" "))
            assertTrue(player.played.isEmpty())
        }

    /** Проверяет сценарий «символы вне Морзе не передаются устройству». */
    @Test
    fun `символы вне Морзе не передаются устройству`() =
        runTest {
            assertValidationError(useCase("..- 2 .-"))
            assertTrue(player.played.isEmpty())
        }

    /** Проверяет сценарий «без вибромотора воспроизведение недоступно». */
    @Test
    fun `без вибромотора воспроизведение недоступно`() =
        runTest {
            player.available = false
            assertFalse(useCase.isAvailable())
            assertTrue(useCase("...").exceptionOrNull() is DomainError.HardwareUnavailable)
            assertTrue(player.played.isEmpty())
        }
}

/** Проверяет [PlayPuzzleUseCase] на заданных входных данных с тестовыми зависимостями. */
class PlayPuzzleUseCaseTest {
    private val repository = FakePuzzleRepository()
    private val useCase = PlayPuzzleUseCase(repository)

    /** Проверяет сценарий «проверяет ответ через репозиторий». */
    @Test
    fun `проверяет ответ через репозиторий`() =
        runTest {
            val result = useCase(1, 2, "SOS")

            assertEquals(true, result.getOrNull())
            assertEquals(listOf<Any?>(1, 2, "SOS"), repository.answerCalls.single())
        }

    /** Проверяет сценарий «некорректные данные возвращают ошибку валидации». */
    @Test
    fun `некорректные данные возвращают ошибку валидации`() =
        runTest {
            assertValidationError(useCase(0, 2, "SOS"))
            assertValidationError(useCase(1, 0, "SOS"))
            assertValidationError(useCase(1, 2, " "))
            assertTrue(repository.answerCalls.isEmpty())
        }
}
