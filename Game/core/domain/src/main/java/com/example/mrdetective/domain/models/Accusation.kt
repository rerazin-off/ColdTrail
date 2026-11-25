package com.example.mrdetective.domain.models

/**
 * Хранит выбранного подозреваемого и серверный вердикт расследования.
 *
 * @property id Идентификатор обвинения.
 * @property caseId Идентификатор дела, к которому относится запись или операция.
 * @property suspectId Идентификатор подозреваемого.
 * @property isCorrect Признак правильного обвинения. Если результат ещё не известен, значение может
 *   отсутствовать.
 * @property explanation Текстовое объяснение вердикта.
 * @property culpritId Идентификатор настоящего виновника, если сервер его раскрыл.
 * @property selectedTruth Объяснение роли обвинённого подозреваемого после вердикта.
 * @property culpritTruth Объяснение роли настоящего виновника, если оно доступно.
 */
data class Accusation(
    val id: Int,
    val caseId: Int,
    val suspectId: Int,
    val isCorrect: Boolean? = null,
    val explanation: String = "",
    val culpritId: Int? = null,
    val selectedTruth: String? = null,
    val culpritTruth: String? = null,
)
