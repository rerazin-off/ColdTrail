package com.example.mrdetective.domain.models

/** Различает игрока и подозреваемого в диалоге. */
enum class MessageAuthor {
    PLAYER,
    SUSPECT,
}

/**
 * Представляет сообщение в диалоге с подозреваемым.
 *
 * @property id Идентификатор реплики.
 * @property caseId Идентификатор дела, к которому относится запись или операция.
 * @property suspectId Идентификатор подозреваемого.
 * @property author Автор реплики: игрок или подозреваемый.
 * @property text Текст реплики игрока или подозреваемого.
 * @property createdAt Время создания записи.
 */
data class DialogueMessage(
    val id: Int,
    val caseId: Int,
    val suspectId: Int,
    val author: MessageAuthor,
    val text: String,
    val createdAt: String,
)
