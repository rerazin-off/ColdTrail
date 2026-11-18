package com.example.mrdetective.data.remote.dto

import com.google.gson.annotations.SerializedName

data class InvestigationInsertDto(@SerializedName("case_id") val caseId: Int)
data class InvestigationDto(val id: Int, @SerializedName("user_id") val userId: String, @SerializedName("case_id") val caseId: Int, val status: String, @SerializedName("start_at") val startedAt: String)
data class PuzzleDto(val id: Int, @SerializedName("case_id") val caseId: Int, val kind: String, val title: String, val hint: String?, @SerializedName("reward_clue") val rewardClueId: Int?)
data class CheckPuzzleDto(@SerializedName("p_investigation") val investigationId: Int, @SerializedName("p_puzzle") val puzzleId: Int, @SerializedName("p_answer") val answer: String)
data class NpcChatRequestDto(@SerializedName("investigation_id") val investigationId: Int, @SerializedName("suspect_id") val suspectId: Int, val text: String, @SerializedName("clue_id") val clueId: Int? = null)
data class NpcChatResponseDto(val reply: String, @SerializedName("message_id") val messageId: Int)
data class AccusationRequestDto(@SerializedName("investigation_id") val investigationId: Int, @SerializedName("suspect_id") val suspectId: Int)
data class AccusationResponseDto(val id: Int, @SerializedName("is_correct") val isCorrect: Boolean, val explanation: String)
