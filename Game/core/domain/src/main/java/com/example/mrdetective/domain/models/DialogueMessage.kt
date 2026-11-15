package com.example.mrdetective.domain.models

enum class MessageAuthor {
    PLAYER,
    SUSPECT
}

data class DialogueMessage(
    val id: Int,
    val caseId: Int,
    val suspectId: Int,
    val author: MessageAuthor,
    val text: String,
    val createdAt: String
)