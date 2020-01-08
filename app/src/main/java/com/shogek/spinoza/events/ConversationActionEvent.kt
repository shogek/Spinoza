package com.shogek.spinoza.events

data class ConversationActionEvent(
    val action: ConversationActions,
    val conversationId: Number
)

enum class ConversationActions {
    ARCHIVE,
    DELETE,
    MUTE,
    UNREAD,
    IGNORE,
    BLOCK
}