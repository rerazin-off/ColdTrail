package com.example.mrdetective.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Тело запроса на начало расследования.
 *
 * @property caseId Идентификатор дела, к которому относится запись или операция.
 */
data class InvestigationInsertDto(
    @SerializedName("case_id") val caseId: Int,
)

/**
 * Запись расследования из Supabase.
 *
 * @property id Идентификатор расследования.
 * @property userId Идентификатор владельца записи или активного игрока.
 * @property caseId Идентификатор дела, к которому относится запись или операция.
 * @property status Текущее состояние дела или расследования.
 * @property startedAt Время начала расследования.
 */
data class InvestigationDto(
    val id: Int,
    @SerializedName("user_id") val userId: String,
    @SerializedName("case_id") val caseId: Int,
    val status: String,
    @SerializedName("start_at") val startedAt: String,
)

/**
 * Публичные метаданные головоломки из Supabase.
 *
 * @property id Идентификатор головоломки.
 * @property caseId Идентификатор дела, к которому относится запись или операция.
 * @property kind Тип головоломки в ответе Supabase.
 * @property title Название дела, улики или головоломки для отображения.
 * @property hint Подсказка к головоломке, если она задана.
 * @property rewardClueId Идентификатор улики, выдаваемой за головоломку.
 */
data class PuzzleDto(
    val id: Int,
    @SerializedName("case_id") val caseId: Int,
    val kind: String,
    val title: String,
    val hint: String?,
    @SerializedName("reward_clue") val rewardClueId: Int?,
)

/**
 * Ответ игрока для проверки головоломки на сервере.
 *
 * @property investigationId Идентификатор расследования. Для локально начатого дела до синхронизации он может
 *   быть отрицательным.
 * @property puzzleId Идентификатор головоломки.
 * @property answer Текст ответа на головоломку.
 */
data class CheckPuzzleDto(
    @SerializedName("p_investigation") val investigationId: Int,
    @SerializedName("p_puzzle") val puzzleId: Int,
    @SerializedName("p_answer") val answer: String,
)

/**
 * Серверная модель сигнала Морзе и состояния головоломки.
 *
 * @property id Идентификатор головоломки Морзе.
 * @property title Название дела, улики или головоломки для отображения.
 * @property hint Подсказка к головоломке, если она задана.
 * @property signal Сигнал Морзе в виде точек, тире и разделителей.
 * @property rewardClueId Идентификатор улики, выдаваемой за головоломку.
 * @property solved Подтверждено ли правильное решение головоломки.
 * @property rewardClaimed Уже ли получена улика за головоломку.
 */
data class MorseChallengeDto(
    val id: Int,
    val title: String,
    val hint: String?,
    val signal: String,
    @SerializedName("reward_clue") val rewardClueId: Int,
    val solved: Boolean,
    @SerializedName("reward_claimed") val rewardClaimed: Boolean,
)

/**
 * Вопрос и необязательная улика, отправляемые в серверную функцию диалога.
 *
 * @property investigationId Идентификатор расследования. Для локально начатого дела до синхронизации он может
 *   быть отрицательным.
 * @property suspectId Идентификатор подозреваемого.
 * @property text Вопрос или реплика игрока для подозреваемого.
 * @property clueId Идентификатор улики.
 */
data class NpcChatRequestDto(
    @SerializedName("investigation_id") val investigationId: Int,
    @SerializedName("suspect_id") val suspectId: Int,
    val text: String,
    @SerializedName("clue_id") val clueId: Int? = null,
)

/**
 * Реплика, возвращаемая серверной функцией диалога.
 *
 * @property reply Текст ответа подозреваемого.
 * @property messageId Серверный идентификатор реплики подозреваемого.
 */
data class NpcChatResponseDto(
    val reply: String,
    @SerializedName("message_id") val messageId: Int,
)

/**
 * Запрос на проверку обвинения в пределах указанного расследования.
 *
 * @property investigationId Идентификатор расследования. Для локально начатого дела до синхронизации он может
 *   быть отрицательным.
 * @property suspectId Идентификатор подозреваемого.
 */
data class AccusationRequestDto(
    @SerializedName("investigation_id") val investigationId: Int,
    @SerializedName("suspect_id") val suspectId: Int,
)

/**
 * Вердикт и объяснение, возвращаемые функцией обвинения.
 *
 * @property id Идентификатор обвинения.
 * @property isCorrect Признак правильного обвинения. Если результат ещё не известен, значение может
 *   отсутствовать.
 * @property explanation Текстовое объяснение вердикта.
 * @property suspectId Идентификатор подозреваемого.
 * @property culpritId Идентификатор настоящего виновника, если сервер его раскрыл.
 * @property selectedTruth Объяснение роли обвинённого подозреваемого после вердикта.
 * @property culpritTruth Объяснение роли настоящего виновника, если оно доступно.
 */
data class AccusationResponseDto(
    val id: Int,
    @SerializedName("is_correct") val isCorrect: Boolean,
    val explanation: String,
    @SerializedName("suspect_id") val suspectId: Int? = null,
    @SerializedName("culprit_id") val culpritId: Int? = null,
    @SerializedName("selected_truth") val selectedTruth: String? = null,
    @SerializedName("culprit_truth") val culpritTruth: String? = null,
)
