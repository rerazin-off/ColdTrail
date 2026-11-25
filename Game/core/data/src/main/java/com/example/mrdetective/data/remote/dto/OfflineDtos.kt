package com.example.mrdetective.data.remote.dto

import com.google.gson.annotations.SerializedName

/** Request body for starting an investigation. */
data class InvestigationInsertDto(
    @SerializedName("case_id") val caseId: Int,
)

/** Investigation record returned by Supabase. */
data class InvestigationDto(
    val id: Int,
    @SerializedName("user_id") val userId: String,
    @SerializedName("case_id") val caseId: Int,
    val status: String,
    @SerializedName("start_at") val startedAt: String,
)

/** Public puzzle metadata returned by Supabase. */
data class PuzzleDto(
    val id: Int,
    @SerializedName("case_id") val caseId: Int,
    val kind: String,
    val title: String,
    val hint: String?,
    @SerializedName("reward_clue") val rewardClueId: Int?,
)

/** Submission containing the answer for a server-validated puzzle. */
data class CheckPuzzleDto(
    @SerializedName("p_investigation") val investigationId: Int,
    @SerializedName("p_puzzle") val puzzleId: Int,
    @SerializedName("p_answer") val answer: String,
)

/** Server representation of a Morse pattern and its challenge state. */
data class MorseChallengeDto(
    val id: Int,
    val title: String,
    val hint: String?,
    val signal: String,
    @SerializedName("reward_clue") val rewardClueId: Int,
    val solved: Boolean,
    @SerializedName("reward_claimed") val rewardClaimed: Boolean,
)

/** Question and optional evidence sent to the suspect chat function. */
data class NpcChatRequestDto(
    @SerializedName("investigation_id") val investigationId: Int,
    @SerializedName("suspect_id") val suspectId: Int,
    val text: String,
    @SerializedName("clue_id") val clueId: Int? = null,
)

/** Reply returned by the suspect chat function. */
data class NpcChatResponseDto(
    val reply: String,
    @SerializedName("message_id") val messageId: Int,
)

/** Selected suspect and evidence sent to the accusation function. */
data class AccusationRequestDto(
    @SerializedName("investigation_id") val investigationId: Int,
    @SerializedName("suspect_id") val suspectId: Int,
)

/** Verdict and explanation returned by the accusation function. */
data class AccusationResponseDto(
    val id: Int,
    @SerializedName("is_correct") val isCorrect: Boolean,
    val explanation: String,
    @SerializedName("suspect_id") val suspectId: Int? = null,
    @SerializedName("culprit_id") val culpritId: Int? = null,
    @SerializedName("selected_truth") val selectedTruth: String? = null,
    @SerializedName("culprit_truth") val culpritTruth: String? = null,
)
