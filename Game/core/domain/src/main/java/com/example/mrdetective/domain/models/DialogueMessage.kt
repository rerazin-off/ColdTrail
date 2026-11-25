package com.example.mrdetective.domain.models

/** Различает игрока и подозреваемого в диалоге. */
enum class MessageAuthor {
    PLAYER,
    SUSPECT,
}

/** Представляет сообщение в диалоге с подозреваемым. */
data class DialogueMessage(
    val id: Int,
    val caseId: Int,
    val suspectId: Int,
    val author: MessageAuthor,
    val text: String,
    val createdAt: String,
)
