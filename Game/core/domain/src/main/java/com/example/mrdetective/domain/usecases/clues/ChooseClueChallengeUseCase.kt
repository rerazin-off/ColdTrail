package com.example.mrdetective.domain.usecases.clues

import com.example.mrdetective.domain.repositories.CameraChallengeRepository
import com.example.mrdetective.domain.repositories.CodeChallengeRepository
import com.example.mrdetective.domain.repositories.MorseChallengeRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

/** Перечисляет типы головоломок, доступных при поиске улик. */
enum class ClueChallengeKind { Morse, Camera, Code }

/**
 * Указывает головоломку, выбранную для следующей попытки добыть улику.
 *
 * @property puzzleId Идентификатор головоломки.
 * @property kind Тип головоломки для выбора маршрута.
 */
data class ClueChallengeChoice(val puzzleId: Int, val kind: ClueChallengeKind)

/**
 * Выбирает доступную головоломку, которую ещё не решили и за которую не получили награду.
 *
 * @param morse Репозиторий головоломок Морзе.
 * @param camera Источник доступных головоломок с камерой.
 * @param code Репозиторий головоломок с цифровым кодом.
 */
class ChooseClueChallengeUseCase
    @Inject
    constructor(
        private val morse: MorseChallengeRepository,
        private val camera: CameraChallengeRepository,
        private val code: CodeChallengeRepository,
    ) {
        /**
         * Возвращает одну доступную головоломку из поддерживаемых типов, если такие остались.
         *
         * @param caseId Идентификатор выбранного дела.
         * @return Доступная головоломка, `null` при полном списке без доступных головоломок или ошибка
         *   загрузки.
         */
        suspend operator fun invoke(caseId: Int): Result<ClueChallengeChoice?> =
            coroutineScope {
                val morseLoad = async { morse.load(caseId) }
                val cameraLoad = async { camera.load(caseId) }
                val codeLoad = async { code.load(caseId) }
                val morseResult = morseLoad.await()
                val cameraResult = cameraLoad.await()
                val codeResult = codeLoad.await()
                val choices =
                    buildList {
                        morseResult.getOrNull()?.puzzles.orEmpty().forEach {
                            if (!it.solved && !it.rewardClaimed) add(ClueChallengeChoice(it.id, ClueChallengeKind.Morse))
                        }
                        cameraResult.getOrNull().orEmpty().forEach {
                            if (!it.pendingOrCompleted && !it.rewardClaimed) add(ClueChallengeChoice(it.id, ClueChallengeKind.Camera))
                        }
                        codeResult.getOrNull()?.puzzles.orEmpty().forEach {
                            if (!it.solved && !it.rewardClaimed) add(ClueChallengeChoice(it.id, ClueChallengeKind.Code))
                        }
                    }
                val choice = choices.randomOrNull()
                val error = cameraResult.exceptionOrNull() ?: morseResult.exceptionOrNull() ?: codeResult.exceptionOrNull()
                if (choice == null && error != null) Result.failure(error) else Result.success(choice)
            }
    }
