package com.example.mrdetective.data.remote.dto

import com.google.gson.annotations.SerializedName

/** Тело запроса на начало расследования. */
data class InvestigationInsertDto(
    @SerializedName("case_id") val caseId: Int,
)

/** Запись расследования из Supabase. */
data class InvestigationDto(
    val id: Int,
    @SerializedName("user_id") val userId: String,
    @SerializedName("case_id") val caseId: Int,
    val status: String,
    @SerializedName("start_at") val startedAt: String,
)

/** Публичные метаданные головоломки из Supabase. */
data class PuzzleDto(
    val id: Int,
    @SerializedName("case_id") val caseId: Int,
    val kind: String,
    val title: String,
    val hint: String?,
    @SerializedName("reward_clue") val rewardClueId: Int?,
)

/** Ответ игрока для проверки головоломки на сервере. */
data class CheckPuzzleDto(
    @SerializedName("p_investigation") val investigationId: Int,
    @SerializedName("p_puzzle") val puzzleId: Int,
    @SerializedName("p_answer") val answer: String,
)

/** Серверная модель сигнала Морзе и состояния головоломки. */
data class MorseChallengeDto(
    val id: Int,
    val title: String,
    val hint: String?,
    val signal: String,
    @SerializedName("reward_clue") val rewardClueId: Int,
    val solved: Boolean,
    @SerializedName("reward_claimed") val rewardClaimed: Boolean,
)

/** Вопрос и необязательная улика, отправляемые в серверную функцию диалога. */
data class NpcChatRequestDto(
    @SerializedName("investigation_id") val investigationId: Int,
    @SerializedName("suspect_id") val suspectId: Int,
    val text: String,
    @SerializedName("clue_id") val clueId: Int? = null,
)

/** Реплика, возвращаемая серверной функцией диалога. */
data class NpcChatResponseDto(
    val reply: String,
    @SerializedName("message_id") val messageId: Int,
)

/** Выбранный подозреваемый и улики для проверки обвинения. */
data class AccusationRequestDto(
    @SerializedName("investigation_id") val investigationId: Int,
    @SerializedName("suspect_id") val suspectId: Int,
)

/** Вердикт и объяснение, возвращаемые функцией обвинения. */
data class AccusationResponseDto(
    val id: Int,
    @SerializedName("is_correct") val isCorrect: Boolean,
    val explanation: String,
    @SerializedName("suspect_id") val suspectId: Int? = null,
    @SerializedName("culprit_id") val culpritId: Int? = null,
    @SerializedName("selected_truth") val selectedTruth: String? = null,
    @SerializedName("culprit_truth") val culpritTruth: String? = null,
)
