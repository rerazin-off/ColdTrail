package com.example.mrdetective.domain.usecases.clues

import com.example.mrdetective.domain.models.CameraChallenge
import com.example.mrdetective.domain.models.CodeChallenge
import com.example.mrdetective.domain.models.CodeChallenges
import com.example.mrdetective.domain.models.MorseChallenges
import com.example.mrdetective.domain.repositories.CameraChallengeRepository
import com.example.mrdetective.domain.repositories.CameraSubmission
import com.example.mrdetective.domain.repositories.CodeChallengeRepository
import com.example.mrdetective.domain.repositories.MorseChallengeRepository
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Проверяет [ChooseClueChallengeUseCase] на заданных входных данных с тестовыми зависимостями. */
class ChooseClueChallengeUseCaseTest {
    private val code = CodeChallenge(7, "Код", "Подсказка", 19, solved = false, rewardClaimed = false)

    /** Проверяет выбор доступного кода и пропуск уже решённых головоломок. */
    @Test
    fun choosesAnAvailableCodeAndSkipsAlreadyResolvedOnes() =
        runTest {
            val solved = code.copy(id = 5, solved = true)
            val claimed = code.copy(id = 6, rewardClaimed = true)
            assertEquals(ClueChallengeChoice(code.id, ClueChallengeKind.Code), picker(listOf(solved, claimed, code))(1).getOrThrow())
            assertNull(picker(listOf(solved, claimed))(1).getOrThrow())
        }

    private fun picker(codes: List<CodeChallenge>) =
        ChooseClueChallengeUseCase(
            morse =
                object : MorseChallengeRepository {
                    override suspend fun load(caseId: Int) = Result.success(MorseChallenges(1, emptyList()))
                },
            camera =
                object : CameraChallengeRepository {
                    override suspend fun load(caseId: Int) = Result.success(emptyList<CameraChallenge>())

                    override suspend fun submit(
                        challenge: CameraChallenge,
                        photoPath: String,
                    ) = Result.success(Unit)

                    override fun observeSubmission(challenge: CameraChallenge) = emptyFlow<CameraSubmission>()

                    override fun observeSubmissionError(challenge: CameraChallenge) = emptyFlow<String?>()
                },
            code =
                object : CodeChallengeRepository {
                    override suspend fun load(caseId: Int) = Result.success(CodeChallenges(1, codes))
                },
        )
}
