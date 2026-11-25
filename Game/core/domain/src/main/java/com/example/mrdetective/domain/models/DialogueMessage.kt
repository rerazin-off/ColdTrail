package com.example.mrdetective.domain.models

/** Distinguishes the player from the suspect in a dialogue. */
enum class MessageAuthor {
    PLAYER,
    SUSPECT,
}

/** Represents one message in an investigation's conversation with a suspect. */
data class DialogueMessage(
    val id: Int,
    val caseId: Int,
    val suspectId: Int,
    val author: MessageAuthor,
    val text: String,
    val createdAt: String,
)
